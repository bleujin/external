package net.ion.external.ics.bean;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAllBean extends TestCase {

	public static TestSuite suite(){
		TestSuite result = new TestSuite();
		
		result.addTestSuite(TestCategoryChildren.class);
		result.addTestSuite(TestSiteCategoryX.class);
		result.addTestSuite(TestArticleChildrenX.class);
		result.addTestSuite(TestArticleX.class);


		result.addTestSuite(TestGalleryCategoryX.class);
		result.addTestSuite(TestGalleryX.class);

		
		
		result.addTestSuite(TestTemplateX.class);
		
		result.addTestSuite(TestUserX.class);
		result.addTestSuite(TestAfieldX.class) ;

		
		// advanced
		result.addTestSuite(TestOutputHandler.class);

		return result ;
	}
}
