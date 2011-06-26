package org.lmnl.xml;

import org.lmnl.QName;

public interface XMLParserConfiguration {

	public abstract boolean isLineElement(QName name);

	public abstract boolean isContainerElement(QName name);

	public abstract boolean included(QName name);

	public abstract boolean excluded(QName name);

	public abstract char getNotableCharacter();

	public abstract boolean isNotable(QName name);

	public abstract boolean isCompressingWhitespace();

}