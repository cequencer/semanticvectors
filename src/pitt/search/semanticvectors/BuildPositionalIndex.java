/**
	 Copyright (c) 2008, University of Pittsburgh

	 All rights reserved.

	 Redistribution and use in source and binary forms, with or without
	 modification, are permitted provided that the following conditions are
	 met:

	 * Redistributions of source code must retain the above copyright
	 notice, this list of conditions and the following disclaimer.

	 * Redistributions in binary form must reproduce the above
	 copyright notice, this list of conditions and the following
	 disclaimer in the documentation and/or other materials provided
	 with the distribution.

	 * Neither the name of the University of Pittsburgh nor the names
	 of its contributors may be used to endorse or promote products
	 derived from this software without specific prior written
	 permission.

	 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
	 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
	 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
	 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
**/

package pitt.search.semanticvectors;

import java.util.LinkedList;
import java.io.IOException;

/**
 * Command line utility for creating semantic vector indexes using the
 * sliding context window approach (see work on HAL, and by Shutze).
 */
public class BuildPositionalIndex{
	/* These can now be modified with command line arguments */
	static int seedLength = 20;
	static int minFreq = 10;
	static int windowLength = 21;

	/**
	 * Prints the following usage message:
	 * <code>
	 * <br> BuildPositionalIndex class in package pitt.search.semanticvectors
	 * <br> Usage: java pitt.search.semanticvectors.BuildPositionalIndex PATH_TO_LUCENE_INDEX
	 * <br> BuildPositionalIndex creates file termtermvectors.bin in local directory.
	 * <br> Other parameters that can be changed include windowlength (size of context window), vector length,
	 * <br>     (number of dimensions), seed length (number of non-zero
	 * <br>     entries in basic vectors), and minimum term frequency.
	 * <br> To change these use the following command line arguments:
	 * <br> -d [number of dimensions]
	 * <br> -s [seed length]
	 * <br> -m [minimum term frequency]
	 * <br> -w [window size]
	 * </code>
	 */
	public static void usage(){
		String usageMessage = "\nBuildPositionalIndex class in package pitt.search.semanticvectors"
			+ "\nUsage: java pitt.search.semanticvectors.BuildPositionalIndex PATH_TO_LUCENE_INDEX"
			+ "\nBuildPositionalIndex creates file termtermvectors.bin  in local directory."
			+ "\nOther parameters that can be changed include vector length,"
			+ "\n windowlength (size of sliding context window),"   
			+ "\n    (number of dimensions), seed length (number of non-zero"
			+ "\n    entries in basic vectors), size of sliding window (including focus term)" 
			+ "\n and minimum term frequency.\n"
			+ "\nTo change these use the command line arguments "
			+ "\n  -d [number of dimensions]"
			+ "\n  -s [seed length]"
			+ "\n  -m [minimum term frequency]"
			+ "\n  -w [window size]";

		System.out.println(usageMessage);
		System.exit(-1);
	}

	/**
	 * Builds term vector stores from a Lucene index - this index must contain TermPositionVectors.
	 * @param args
	 * @see BuildPositionalIndex#usage
	 */
	public static void main (String[] args) {
		boolean wellFormed = false;
		/* If only one argument, it should be the path to Lucene index. */
		if (args.length == 1) {
			wellFormed = true;
		}
		/* If there is an even number of arguments, there's a problem. */
		else if (args.length % 2 == 0) {
			wellFormed = false;
		}
		/* Parse command line arguments. */
		else {
			for (int x = 0; x < args.length-1; x += 2) {
				String pa = args[x];
				String ar = args[x+1];

				/* Get number of dimensions. */
				if (pa.equalsIgnoreCase("-d")) {
					try {
						ObjectVector.vecLength = Integer.parseInt(ar);
						wellFormed = true;
					} catch (NumberFormatException e) {
						System.err.println(ar + " is not a number"); usage();
					}
				}
				/* Get seedlength. */
				else if (pa.equalsIgnoreCase("-s")) {
					try {
						seedLength = Integer.parseInt(ar);
						if (seedLength > ObjectVector.vecLength) {
							System.err.println("Seed length cannot be greater than vector length");
							usage();
						}
						else wellFormed = true;
					} catch (NumberFormatException e) {
						System.err.println(ar + " is not a number"); usage();
					}
				}
				/* Get minimum term frequency. */
				else if (pa.equalsIgnoreCase("-m")) {
					try {
						minFreq = Integer.parseInt(ar);
						if (minFreq < 0) {
							System.err.println("Minimum frequency cannot be less than zero");
							usage();
						}
						else wellFormed = true;
					} catch (NumberFormatException e) {
						System.err.println(ar + " is not a number"); usage();
					}
				}
				/* Get window size */
				else if (pa.equalsIgnoreCase("-w")) {
					try {
						windowLength = Integer.parseInt(ar);
						if ((windowLength <= 2) |  (windowLength %2 == 0)  ) {
							System.err.println("Windowlength must be an odd number " + 
																 "(to accommodate a central focus term), larger than 2");
							usage();
						}
						else wellFormed = true;
					} catch (NumberFormatException e) {
						System.err.println(ar + " is not a number"); usage();
					}
				}
				/* All other arguments are unknown. */
				else {
					System.err.println("Unknown command line option: " + pa);
					usage();
				}
			}
		}
		if (!wellFormed) {
			usage();
		}

		String luceneIndex = args[args.length-1];
		String termFile = "termtermvectors.bin";
		String[] fieldsToIndex = {"contents"};
		System.err.println("seedLength = " + seedLength);
		System.err.println("Vector length = " + ObjectVector.vecLength);
		System.err.println("Minimum frequency = " + minFreq);
		System.err.println("Window length = " + windowLength);
		try{
			TermTermVectorsFromLucene vecStore =
				new TermTermVectorsFromLucene(luceneIndex, seedLength, minFreq, windowLength, fieldsToIndex);
			VectorStoreWriter vecWriter = new VectorStoreWriter();
			System.err.println("Writing term vectors to " + termFile);
			vecWriter.WriteVectors(termFile, vecStore);
					
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}