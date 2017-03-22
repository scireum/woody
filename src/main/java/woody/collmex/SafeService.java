package woody.collmex;

public interface SafeService {

	/**
	 * decodes the given encoded text into a decoded text using the given
	 * keyword
	 * 
	 */
	public String decodeByKey(String keyword, String encodedText);

	/**
	 * encodes the given text into a encoded text using the given keyword
	 * 
	 */
	public String encodeByKey(String keyword, String text);

	public void writeKeyword(String keyword, String group);

	public String readKeyword(String group);

	public void forgetKeyForGroup(String group);

}
