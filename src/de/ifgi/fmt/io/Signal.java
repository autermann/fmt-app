package de.ifgi.fmt.io;

/**
 * http://giv-flashmob.uni-muenster.de/schema/signal
 * 
 * @author Matthias Robbers
 */
public class Signal {
	private String id;
	private String text;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	};

}
