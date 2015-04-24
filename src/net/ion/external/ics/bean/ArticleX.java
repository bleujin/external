package net.ion.external.ics.bean;

import com.google.common.collect.Lists;
import net.ion.cms.rest.sync.Def;
import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteSession;
import net.ion.craken.tree.PropertyId;
import net.ion.craken.tree.PropertyId.PType;
import net.ion.external.domain.Domain;
import net.ion.framework.parse.gson.GsonBuilder;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.SetUtil;
import net.ion.framework.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
		return AfieldValueX.create(domain(), session().ghostBy("/datas/avalue/" + artId() + "/" + afieldId), catId(), artId());
	}
	
	public XIterable<AfieldValueX> afields(){
		return XIterable.<AfieldValueX>create(domain(), session().ghostBy("/datas/avalue/" + artId()).children().toList(), AfieldValueX.class) ;
	}
	
	public XIterable<AfieldValueX> afields(boolean onlySetted) throws IOException{
		if(! onlySetted) return afields() ;
		
		XIterable<AfieldMetaX> setted = category().afieldMetas() ;
		
		
		List<ReadNode> result = ListUtil.newList() ;
		List<ReadNode> haveafield = session().ghostBy("/datas/avalue/" + artId()).children().toList() ;
		
		List<String> haveIdForResult = ListUtil.newList() ;
		for(ReadNode node : haveafield){
			if (setted.hasKey(node.property("afieldid").asString())) {
				result.add(node) ; 
				haveIdForResult.add(node.property("afieldid").asString()) ;
			}
		}
		
		for(AfieldMetaX afield : setted){
			if (! haveIdForResult.contains(afield.afieldId())){
				result.add(session().ghostBy("/datas/avalue/" + artId() + "/" + afield.afieldId())) ;
			}
		}
		
		
		return XIterable.<AfieldValueX>create(domain(), result, AfieldValueX.class) ; 
	}


	
	public SiteCategoryX category() throws IOException {
		return domain().datas().scategory(catId());
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

	public UserX regUser() {
		return UserX.create(domain(), session().ghostBy(Def.User.pathBy(asString("reguserid"))));
	}

	public String asStreamPath(String name) {
		return "/thumbimg/" + catId()  + "/" + artId() + ".stream";
	}

	public String content(){
		String content = asString("content") ;
		
		
		String[] founds = StringUtil.substringsBetween(content, "[[--ArtInImage,fileLoc:", "--]]") ;
		if (founds == null) return content ;
		List<String> searchs = ListUtil.newList() ;
		List<String> replaces = ListUtil.newList() ;
		for (String found : founds) {
			searchs.add("[[--ArtInImage,fileLoc:" + found + "--]]") ;
            ///{did}/content/{catid}/{artid}/{resourceid}.stream
			replaces.add("/admin/article/" + domainId() + "/content/" + catId() + "/" + artId() + "/img" + found.toLowerCase().hashCode() + ".stream" ) ;
		}
		String transed = StringUtil.replaceEach(content, searchs.toArray(new String[0]), replaces.toArray(new String[0])) ;
		
		
		String[] gfounds = StringUtil.substringsBetween(transed, "/ics/galm/gallery.do?forwardName=resource_view&galId=", "\"");
        List<String> gsearchs = ListUtil.newList();
        List<String> greplaces = ListUtil.newList();

        if(gfounds != null) {
            for (String found : gfounds) {
                gsearchs.add("/ics/galm/gallery.do?forwardName=resource_view&galId=" + found);
                // /{did}/content/{catid}/{artid}/{resourceid}.stream
                greplaces.add("/admin/gallery/" + domainId() + "/view/" + found);
            }
        }

		return StringUtil.replaceEachRepeatedly(transed, gsearchs.toArray(new String[0]), greplaces.toArray(new String[0])) ;
	}
	
	public void jsonWrite(Writer writer) throws IOException {
        JsonObject result = JsonObject.create() ;
        
        result.put("std_info", JsonObject.create().put("artid", artId()).put("reguserid", asString("reguserid")).put("subject", asString("subject")).put("content", content())) ;
        result.put("add_info", JsonObject.create().put("hasthumb", !asString("thumbimg").isEmpty()).put("thumbimg", !asString("thumbimg").isEmpty() ? asStreamPath("thumbimg") : "").put("keyword", asString("keyword"))) ;
        result.put("set_info", JsonObject.create().put("operday", asString("operday")).put("expireday", asString("expireday")).put("gourlloc", asString("gourlloc")).put("priority", asString("priority")).put("artfilenm", asString("artfilenm"))) ;
        
        JsonObject afields = JsonObject.create() ;
        for(AfieldValueX avalue : afields(false).toList()){
        	afields.put(avalue.afieldId(), avalue.asString()) ;
        	result.put("afield." + avalue.afieldId() , new JsonObject().put("typecd", avalue.typeCd()).put("stringvalue", avalue.asString()).put("streamvalue", avalue.asStreamPath())) ;
        }
        result.put("afields", afields) ;
        
		writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(result));
	}


}
