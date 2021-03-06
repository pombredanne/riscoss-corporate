package eu.riscoss.db.domdb;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class NameAttributeProvider implements AttributeProvider<String> {
	@Override
	public String getValue( ODocument doc ) {
		return doc.field( "tag" );
	}
}