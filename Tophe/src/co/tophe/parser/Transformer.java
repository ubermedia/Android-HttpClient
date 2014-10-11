package co.tophe.parser;

import java.io.IOException;

import co.tophe.ImmutableHttpRequest;

/**
 * A more generic {@link XferTransform} that doesn't throw {@link java.io.IOException} or {@link ParserException}
 * to be used when the network data are handled and only post-processing is needed
 *
 * @author Created by robUx4 on 21/08/2014.
 */
public abstract class Transformer<INPUT, OUTPUT> implements XferTransform<INPUT, OUTPUT> {

	/**
	 * Transform the data from {@link INPUT} to {@link OUTPUT}
	 * @param input Input data
	 * @return Transformed data
	 */
	protected abstract OUTPUT transform(INPUT input);

	@Deprecated
	@Override
	public final OUTPUT transformData(INPUT input, ImmutableHttpRequest request) throws IOException, ParserException {
		return transform(input);
	}
}