package objectmapper.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;

import au.com.bytecode.opencsv.CSVReader;

public class CsvResource implements Resource {
	
	//private CSVReader reader;
	private String[] columns;
	private HashMap<String, Integer> keys;
	//private String[] values, nextValues;
	
	private Reader reader;
	private File file;
	
	public CsvResource(String fileName) throws IOException{
		 //reader = new CSVReader(new FileReader(fileName));
		//reader = new FileReader(fileName);
		//csvReader를 본 결과. file은 상태 공유 안됨.
		file = new File(fileName);
		 
		 columns = new CSVReader(new FileReader(file)).readNext();
		 keys = new HashMap<String, Integer>();
		 for(int i = 0; i < columns.length; i ++){
			 keys.put(columns[i], i);
		 }
	}

	public Iterator<Data> iterator() {
		try {
			return new Iterator<Data>() {

				//같은 fileReader를 사용하는 부분이 문제. fileReader는 또 bufferedReader로 싸여있음.
				private CSVReader csvReader = new CSVReader(new FileReader(file));
				
				//상태를 독립적으로 만들기 위해. 안에 넣었음. 위에 있으면 상태가 공유.
				private String[] values, nextValues;
				
				{
					//익명클래스의 생성자 부분.
					//첫 줄은 데이터가 아니니까 버림.
					//위의 HashMap을 만드는 것과 같음. 하지만 columns와 keys는 iterator마다 가질
					//필요가 없기 때문에 그냥 이렇게.
					csvReader.readNext();
				}

				//try catch를 안하고 throw 하면 리스코프 대체원칙 위반임.
				//iterator는 ioexception은 throw하지 않음.
				//lookahead 구현. 앞에꺼를 미리 하나 더 본 것.
				//csvreader가 next를 제공 안해서 그런거임.
				public boolean hasNext() {
					if(nextValues == null){
						try {
							nextValues = csvReader.readNext(); // 현재 상태에 따라서 다른 값이 나옴. 상태 객체
						} catch (IOException e) {
							return false;
							//이 부분이 없어도 마찬가지임. 어차피 nextValues는 null이니까.
						}
					}
					return !(nextValues == null);
				}

				public Data next() {
					if(!hasNext()){
						return null;
					}
					values = nextValues;
					nextValues = null;
					
					return new Data() {
						
						public boolean hasKey(String key) {
							return keys.containsKey(key);
						}
						
						public Object get(String key) {
							return keys.containsKey(key) ? values[keys.get(key)] : null;
						}
					};
				}

				public void remove() {
				}
			};
		} catch (IOException e) {
			return null;
		}
	}
}
