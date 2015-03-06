package net.ion.external.ics.setup;

import net.ion.craken.node.IteratorList;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.WriteChildren;
import net.ion.framework.util.Debug;

public class TestTemplateSetup extends TestSetup{

	
	public void testTemplate() throws Exception {
		String procSQL ="select '/template/' || catId || '/' || tplId fqn, catId, tplId, tplNm name, tplExp explain, tplKindCd kindCd, tplTypeCd typeCd, listfilenm filename, regUserId  regUserId " + 
						" from template_tblc " +  
						" where isRemoved = 'F' and useFlg = 'T'";
		
		Debug.line(saveToCraken(procSQL) + " applied"); 
	}
	
	
	
	
	public void xtestViewTemplate() throws Exception {
		session.pathBy("/template").walkChildren().debugPrint();
	}

	public void xtestViewUser() throws Exception {
		session.pathBy("/user").walkChildren().debugPrint();
	}

}
