package martin.chess.strategy;

public class Pair<T, V> {
	public T first;
	public V second;
	
	public Pair(T first, V second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", first, second);
	}
}