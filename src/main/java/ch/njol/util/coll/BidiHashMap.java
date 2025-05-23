package ch.njol.util.coll;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated Use {@link com.google.common.collect.BiMap} instead.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
@SuppressWarnings("removal")
public class BidiHashMap<T1, T2> extends HashMap<T1, T2> implements BidiMap<T1, T2> {

	private static final long serialVersionUID = -9011678701069901061L;

	private final BidiHashMap<T2, T1> other;

	private BidiHashMap(final BidiHashMap<T2, T1> other) {
		this.other = other;
	}

	public BidiHashMap() {
		other = new BidiHashMap<>(this);
	}

	public BidiHashMap(final Map<? extends T1, ? extends T2> values) {
		other = new BidiHashMap<>(this);
		putAll(values);
	}

	@Override
	public BidiHashMap<T2, T1> getReverseView() {
		return other;
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	public T1 getKey(final T2 value) {
		return other.get(value);
	}

	@SuppressWarnings("null")
	@Override
	@Nullable
	public T2 getValue(final @Nullable T1 key) {
		return get(key);
	}

	@Nullable
	private T2 putDirect(final @Nullable T1 key, final @Nullable T2 value) {
		return super.put(key, value);
	}

	@Override
	@Nullable
	public T2 put(final @Nullable T1 key, final @Nullable T2 value) {
		if (key == null || value == null)
			throw new NullPointerException("Can't store null in a BidiHashMap");

		removeDirect(key);
		other.removeDirect(value);
		final T2 oldValue = putDirect(key, value);
		other.putDirect(value, key);
		return oldValue;
	}

	@SuppressWarnings("null")
	@Override
	public void putAll(final Map<? extends T1, ? extends T2> m) {
		for (final Entry<? extends T1, ? extends T2> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Nullable
	private T2 removeDirect(final @Nullable Object key) {
		return super.remove(key);
	}

	@Override
	@Nullable
	public T2 remove(final @Nullable Object key) {
		final T2 oldValue = removeDirect(key);
		if (oldValue != null)
			other.removeDirect(oldValue);
		return oldValue;
	}

	private void clearDirect() {
		super.clear();
	}

	@Override
	public void clear() {
		this.clearDirect();
		other.clearDirect();
	}

	@Override
	public boolean containsValue(final @Nullable Object value) {
		return other.containsKey(value);
	}

	// TODO check how changes to the sets affect the map

	@SuppressWarnings("null")
	@Override
	public Set<Entry<T1, T2>> entrySet() {
		return Collections.unmodifiableSet(super.entrySet());
	}

	@SuppressWarnings("null")
	@Override
	public Set<T1> keySet() {
		return Collections.unmodifiableSet(super.keySet());
	}

	@Override
	public Set<T2> values() {
		return valueSet();
	}

	@SuppressWarnings("null")
	@Override
	public Set<T2> valueSet() {
		return Collections.unmodifiableSet(other.keySet());
	}

	@Override
	public BidiHashMap<T1, T2> clone() {
		return new BidiHashMap<T1, T2>(this);
	}

}
