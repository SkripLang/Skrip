package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Parse")
@Description({"Parses text as a given type, or as a given pattern.",
		"This expression can be used in two different ways: One which parses the entire text as a single instance of a type, e.g. as a number, " +
				"and one that parses the text according to a pattern.",
		"If the given text could not be parsed, this expression will return nothing and the <a href='#ExprParseError'>parse error</a> will be set if some information is available.",
		"Some notes about parsing with a pattern:",
		"- The pattern must be a <a href='./patterns/'>Skript pattern</a>, " +
				"e.g. percent signs are used to define where to parse which types, e.g. put a %number% or %items% in the pattern if you expect a number or some items there.",
		"- You <i>have to</i> save the expression's value in a list variable, e.g. <code>set {parsed::*} to message parsed as \"...\"</code>.",
		"- The list variable will contain the parsed values from all %types% in the pattern in order. If a type was plural, e.g. %items%, the variable's value at the respective index will be a list variable," +
				" e.g. the values will be stored in {parsed::1::*}, not {parsed::1}."})
@Examples({
	"set {var} to line 1 parsed as number",
	"on chat:",
	"\tset {var::*} to message parsed as \"buying %items% for %money%\"",
	"\tif parse error is set:",
	"\t\tmessage \"%parse error%\"",
	"\telse if {var::*} is set:",
	"\t\tcancel event",
	"\t\tremove {var::2} from the player's balance",
	"\t\tgive {var::1::*} to the player"
})
@Since("2.0")
public class ExprParse extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprParse.class, Object.class, ExpressionType.COMBINED,
			"%string% parsed as (%-*classinfo%|\"<.*>\")");
	}

	@Nullable
	static String lastError = null;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> text;

	@Nullable
	private SkriptPattern pattern;
	@Nullable
	private NonNullPair<ClassInfo<?>, Boolean>[] patternExpressions;
	private boolean single = true;
	public boolean flatten = true;

	@Nullable
	private ClassInfo<?> classInfo;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		text = (Expression<String>) exprs[0];
		if (exprs[1] == null) {
			String pattern = ChatColor.translateAlternateColorCodes('&', parseResult.regexes.get(0).group());

			if (!VariableString.isQuotedCorrectly(pattern, false)) {
				Skript.error("Invalid amount and/or placement of double quotes in '" + pattern + "'");
				return false;
			}

			NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> p = SkriptParser.validatePattern(pattern);
			if (p == null) {
				// Errored in validatePattern already
				return false;
			}

			// Make all types in the pattern plural
			pattern = p.getFirst();
			patternExpressions = p.getSecond();

			// Check if all the types can actually parse
			for (NonNullPair<ClassInfo<?>, Boolean> patternExpression : patternExpressions) {
				if (!canParse(patternExpression.getFirst()))
					return false;
				if (patternExpression.getSecond())
					single = false;
			}

			// Escape '¦' and ':' (used for parser tags/marks)
			pattern = escapeParseTags(pattern);

			// Compile the SkriptPattern
			try {
				this.pattern = PatternCompiler.compile(pattern);
			} catch (MalformedPatternException exception) {
				// Some checks already done by validatePattern above, but just making sure
				Skript.error("Malformed pattern: " + exception.getMessage());
				return false;
			}

			// If the pattern contains at most 1 type, and this type is single, this expression is single
			if (single)
				this.single = this.pattern.countNonNullTypes() <= 1;
		} else {
			classInfo = ((Literal<ClassInfo<?>>) exprs[1]).getSingle();

			if (classInfo.getC() == String.class) {
				Skript.error("Parsing as text is useless as only things that are already text may be parsed");
				return false;
			}

			// Make sure the ClassInfo has a parser
			return canParse(classInfo);
		}
		return true;
	}

	@Override
	@Nullable
	@SuppressWarnings("null")
	protected Object[] get(Event event) {
		String text = this.text.getSingle(event);
		if (text == null)
			return null;

		ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler();
		try {
			lastError = null;

			if (classInfo != null) {
				Parser<?> parser = classInfo.getParser();
				assert parser != null; // checked in init()

				// Parse and return value
				Object value = parser.parse(text, ParseContext.PARSE);
				if (value != null) {
					Object[] valueArray = (Object[]) Array.newInstance(classInfo.getC(), 1);
					valueArray[0] = value;
					return valueArray;
				}
			} else {
				assert pattern != null && patternExpressions != null;

				MatchResult matchResult = pattern.match(text, SkriptParser.PARSE_LITERALS, ParseContext.PARSE);

				if (matchResult != null) {
					Expression<?>[] exprs = matchResult.getExpressions();

					assert patternExpressions.length == exprs.length;

					if (flatten) {
						List<Object> values = new ArrayList<>();
						for (int i = 0; i < exprs.length; i++) {
							if (exprs[i] != null) {
								if (patternExpressions[i].getSecond()) {
									values.addAll(Arrays.asList(exprs[i].getArray(null)));
									continue;
								}
								values.add(exprs[i].getSingle(null));
							}
						}
						return values.toArray();
					}

					int nonNullExprCount = 0;
					for (Expression<?> expr : exprs) {
						if (expr != null) // Ignore missing optional parts
							nonNullExprCount++;
					}

					Object[] values = new Object[nonNullExprCount];

					int valueIndex = 0;
					for (int i = 0; i < exprs.length; i++) {
						if (exprs[i] != null) {
							//noinspection DataFlowIssue
							values[valueIndex] = patternExpressions[i].getSecond() ? exprs[i].getArray(null) : exprs[i].getSingle(null);
							valueIndex++;
						}
					}

					return values;
				}
			}

			LogEntry error = parseLogHandler.getError();
			if (error != null) {
				lastError = error.toString();
			} else {
				if (classInfo != null) {
					lastError = text + " could not be parsed as " + classInfo.getName().withIndefiniteArticle();
				} else {
					lastError = text + " could not be parsed as \"" + pattern + "\"";
				}
			}
			return null;
		} finally {
			parseLogHandler.clear();
			parseLogHandler.printLog();
		}
	}

	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public Class<?> getReturnType() {
		if (classInfo != null)
			return classInfo.getC();
		return patternExpressions.length == 1 ? patternExpressions[0].getFirst().getC() : Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return text.toString(event, debug) + " parsed as " + (classInfo != null ? classInfo.toString(Language.F_INDEFINITE_ARTICLE) : pattern);
	}

	private static boolean canParse(ClassInfo<?> classInfo) {
		Parser<?> parser = classInfo.getParser();
		if (parser == null || !parser.canParse(ParseContext.PARSE)) {
			Skript.error("Text cannot be parsed as " + classInfo.getName().withIndefiniteArticle());
			return false;
		}
		return true;
	}

	/**
	 * Escapes parse tags & parse mark characters in the given pattern.
	 */
	private static String escapeParseTags(String pattern) {
		StringBuilder b = new StringBuilder(pattern.length());

		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);

			if (c == '\\') {
				// Escape character, add it and directly add the next character
				b.append(c);
				b.append(pattern.charAt(i + 1));
				i++;
			} else if (c == '¦' || c == ':') {
				// Escape parse tags & marks
				b.append("\\");
				b.append(c);
			} else {
				// Add regular characters
				b.append(c);
			}
		}

		return b.toString();
	}

}
