package net.ion.external.ics.web.domain;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.StringUtil;

public class TestRegular extends TestCase {
	
	public void testContent() throws Exception {
		String content = "\u003cp\u003e1번\u003c/p\u003e\r\n\u003cp\u003e\u003cimg style\u003d\"vertical-align: baseline; width: 157px; height: 104px;\" alt\u003d\"\" title\u003d\"\" src\u003d\"[[--ArtInImage,fileLoc:/2014/12/12/ibr_test/20140617123603969.jpg--]]\"\u003e\u003cbr\u003e\u003c/p\u003e\r\n\u003cp\u003e2번\u003c/p\u003e\r\n\u003cp\u003e\u003cimg style\u003d\"vertical-align: baseline; width: 200px; height: 313px;\" alt\u003d\"\" title\u003d\"\" src\u003d\"[[--ArtInImage,fileLoc:/2014/12/12/ibr_test/20140624134704329.jpg--]]\"\u003e\u003cbr\u003e\r\n3번\u003c/p\u003e\r\n\u003cp\u003e\u003cimg style\u003d\"vertical-align: baseline; width: 200px; height: 150px;\" alt\u003d\"\" title\u003d\"\" src\u003d\"[[--ArtInImage,fileLoc:/2014/12/12/ibr_test/IMG_1695.JPG--]]\"\u003e\u003c/p\u003e\r\n\u003cp\u003e\u003cbr\u003e\u003c/p\u003e\r\n\u003cp\u003e\u003cbr\u003e\u003c/p\u003e" ;
		
		String[] founds = StringUtil.substringsBetween(content, "[[--ArtInImage,fileLoc:", "--]]") ;
		List<String> searchs = ListUtil.newList() ;
		List<String> replaces = ListUtil.newList() ;
		for (String found : founds) {
			searchs.add("[[--ArtInImage,fileLoc:" + found + "--]]") ;
			replaces.add(found) ;
		}
		
		Debug.line(StringUtil.replaceEach(content, searchs.toArray(new String[0]), replaces.toArray(new String[0]))) ;
	}

}
