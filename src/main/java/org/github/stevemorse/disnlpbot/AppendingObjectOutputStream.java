package org.github.stevemorse.disnlpbot;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
/**
 * The AppendingObjectOutputStream overrides the writeStreamHeader method so that it does not write 
 * a new file header when used to write again to a serialized file
 * @author Steve Morse
 * @version 1.0
 */
public class AppendingObjectOutputStream extends ObjectOutputStream {
	/**
	 * The constructor
	 * @param out an OutputStream
	 * @throws IOException if cannot build
	 */
	public AppendingObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}
	/**
	 * overrides the OutputStream writeStreamHeader() method to avoid a new file header on appending
	 * @throws IOException if cannot use super method
	 */
	@Override 
	protected void writeStreamHeader() throws IOException {  
	    super.writeStreamHeader();
	}  
}//class AppendingObjectOutputStream
