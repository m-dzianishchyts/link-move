package com.nhl.link.move.extractor.parser;

import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class BaseParserTest {

	private DocumentBuilderFactory domFactory;

	@BeforeEach
	public void setupDomFactory() {
		this.domFactory = DocumentBuilderFactory.newInstance();
		this.domFactory.setNamespaceAware(true);
	}

	protected Element getXmlRoot(String resource) {

		try (InputStream in = getClass().getResourceAsStream(resource);) {
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			return domBuilder.parse(new InputSource(in)).getDocumentElement();
		} catch (IOException e) {
			throw new RuntimeException("Error reading resource: " + resource, e);
		} catch (SAXException | ParserConfigurationException e) {
			throw new RuntimeException("Error parsing resource to DOM: " + resource, e);
		}
	}
}
