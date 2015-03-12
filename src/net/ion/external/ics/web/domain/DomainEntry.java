package net.ion.external.ics.web.domain;

import java.io.Closeable;
import java.io.IOException;

import net.ion.external.domain.DomainSub;

import org.apache.lucene.index.CorruptIndexException;

public class DomainEntry implements Closeable{

	private DomainSub dsub;
	public final static String EntryName = "dentry" ;
	
	public DomainEntry(DomainSub dsub) {
		this.dsub = dsub ;
	}

	public static DomainEntry test(DomainSub dsub) {
		DomainEntry result = new DomainEntry(dsub);
		return result;
	}

	@Override
	public void close() throws IOException {
		dsub.craken().stop() ;
	}

	public DomainSub dsub() {
		return dsub;
	}

	
}
