package objectmapper.data;

import java.util.Iterator;
import java.util.Map;

//순회의 책임을 resource로 분리.
public interface Resource extends Iterable<Data> {
	
	public static class Factory	{
		public static Resource fromMaps(final Iterable<Map<String, ?>> maps){
			return new Resource() {
				
				public Iterator<Data> iterator() {
					return new Iterator<Data>() {
						Iterator<Map<String, ?>> iterator = maps.iterator();

						public boolean hasNext() {
							return iterator.hasNext();
						}

						public Data next() {
							return Data.Factory.fromMap(iterator.next());
						}

						public void remove() {
							iterator.remove();
						}
					};
				}
			};
		}
	}

}
