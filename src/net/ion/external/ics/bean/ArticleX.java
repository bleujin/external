package net.ion.external.ics.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.PropertyId;
import net.ion.craken.tree.PropertyId.PType;
import net.ion.external.domain.Domain;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.SetUtil;

public class ArticleX extends BeanX{

	private ArticleX(Domain domain, ReadNode node) {
		super(domain, node);
	}

	public static ArticleX create(Domain domain, ReadNode node) {
		return new ArticleX(domain, node);
	}

	public int artId() {
		return asInt(Def.Article.ArtId);
	}

	public String catId() {
		return asString(Def.Article.CatId);
	}
	
	public AfieldValueX asAfield(String afieldId) {
		return AfieldValueX.create(domain(), session().ghostBy("/datas/avalue/" + artId() + "/" + afieldId));
	}
	
	public XIterable<AfieldValueX> afields(){
		return null ;
	}


	
	public SiteCategoryX category() throws IOException {
		return domain().scategory(catId());
	}
	
	public int increasCount(final String propId) throws InterruptedException, ExecutionException{
		return session().tran(new TransactionJob<Integer>() {
			@Override
			public Integer handle(WriteSession wsession) throws Exception {
				return wsession.pathBy(node().fqn()).increase(propId).intValue(1) ;
			}
		}).get() ;
	}
	
	public String toString(){
		return "artId:" + artId() + ", catId:" + catId() ; 
	}

	public XIterable<ArticleX> relateds() {
//		final IteratorList<ReadNode> relateds = node().refChildren(Article.Related).iterator() ;
		final IteratorList<ReadNode> relateds = node().refChildren("related").iterator() ;
		return XIterable.create(domain(), relateds.toList(), MapUtil.<String, String>newMap(), ArticleX.class) ;
	}

	public Set<String> propKeys() {
		Set<String> result = SetUtil.newSet() ;
		for(PropertyId pid : node().keys()){
			if (pid.type() == PType.REFER) continue ;
			result.add(pid.getString()) ;
		}
		return result ;
	}

	public String fileName(int tplId) {
		return asBoolean(Def.Article.IsUsingUrlLoc) ? asString(Def.Article.ArtFileNm, tplId + "_" + artId() + ".html") : tplId + "_" + artId() + ".html";
	}

	public InputStream thumbnailStream() throws IOException {
		return asStream("thumbimg");
	}

	public InputStream contentStream(String path) throws IOException {
		return asStream("img" + path.toLowerCase().hashCode());
	}


}
