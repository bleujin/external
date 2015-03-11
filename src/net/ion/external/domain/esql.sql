	category
	select '/datas/scat/' || catId fqn,  catId, catnm name, upperCatId parent, phyDirNm, catExp explain, charSetCd, catUrl, orderLnNo, fileServerUrl
	from category_tblc connect by isRemoved = 'F' and  :includeSub = 'T' and prior  catId = upperCatId start with catId = :catId
  
  
	category_afield
	select '/datas/scat/' || x0.catId fqn, '/datas/afield/' || x1.afieldId tfqn, x1.orderLnNo
	from (select catId from category_tblc connect by isRemoved = 'F' and  :includeSub = 'T' and prior  catId = upperCatId start with catId = :catId) x0, 
	    category_afield_tblc x1
	where x0.catId = x1.catId	  

  	articles
	select 
	      '/datas/article/' || x1.catId || '/' || x2.artId fqn, 
	      x1.catId, x1.operday, x1.expireday, x2.artid, x2.modserno, orderno, artfilenm, partid, priority, status action, moduserid, isusingurlloc, keywords keyword, artcont content, gourlloc, creUserId reguserid, artsubject subject, 
	      to_char(x2.modDate, 'yyyymmdd-hh24miss') modday, to_char(x2.creDate, 'yyyymmdd-hh24miss')  creday
	      -- catnmpath, catidpath, month, year, 
	from  (select catId from category_tblc connect by isRemoved = 'F' and :includeSub = 'T' and prior  catId = upperCatId start with catId = :catId) x0, 
	      category_article_tblc x1, article_tblc x2
	where 
	     x0.catId = x1.catId
	     and x1.artId = x2.artId 
	     and x1.useFlg = 'T'
	     and x2.isUsing = 'T' and x2.isSignCmpl = 'T'


	articles_image
	select '/datas/article/' || x1.catId || '/' || x2.artId fqn,  x3.*
	from (select catId from category_tblc connect by isRemoved = 'F' and  :includeSub = 'T' and prior  catId = upperCatId start with catId = :catId) x0, 
    category_article_tblc x1, article_tblc x2, article_img_tblc x3
	where 
        x0.catId = x1.catId
        and x1.artId = x2.artId 
        and x2.artId = x3.artId
        and x2.modSerNo = x3.modSerNo
        and x2.isUsing = 'T' and x2.isSignCmpl = 'T'


	template
	select '/datas/template/' || x0.catId || '/' || tplId fqn, x0.catId, tplId, tplNm name, tplExp explain, tplKindCd kindCd, tplTypeCd typeCd, listfilenm filename, regUserId  regUserId 
	from (select catId from category_tblc connect by isRemoved = 'F' and :includeSub = 'T' and prior  catId = upperCatId start with catId = :catId) x0,  template_tblc t1
	where x0.catId = t1.catId and isRemoved = 'F' and useFlg = 'T'

	
	gcategory
	select '/datas/gcat/' || galcatId fqn , galcatid catid, galuppercatid parent, galcatnm name, reguserid from gallery_category_tblc
	connect by :includeSub = 'T' and galUpperCatId = prior galCatId start with galCatId = :catId

	
	gallery
	select '/datas/gallery/' || t0.galCatId || '/' || galId fqn, galId, t0.galCatId catId, filenm filename, filesize, width, height, typeCd, regUserId, to_char(regDate, 'yyyymmdd-hh24miss') regDate, subject, content, filemeta
	from (select galcatId from gallery_category_tblc connect by :includeSub = 'T' and galUpperCatId = prior galCatId start with galCatId = :catId) t0, gallery_tblc t1
	where t0.galCatId = t1.galcatId and isRemoved= 'F'
	

	articles_afield
	select  '/datas/article/' || x1.catId  || '/'|| x1.artId articleFqn,  '/datas/avalue/'|| '/'||x1.artId || '/' || x3.afieldId fqn, x1.catId, x1.artId, x3.afieldId, x3.typeCd, x3.dvalue, x3.hashvalue, x3.clobvalue, x3.subvalue
    from (select catId from category_tblc connect by isRemoved = 'F' and  :includeSub = 'T' and prior  catId = upperCatId start with catId = :catId)  x0, category_article_tblc x1, article_tblc x2, afield_content_tblc x3
    where
        x0.catId = x1.catId
        and x1.artId = x2.artId
        and x2.artId = x3.artId
        and x2.modSerNo = x3.modSerNo
        and x2.isUsing = 'T' and x2.isSignCmpl = 'T'


	afield
    select '/datas/afield/' || x1.afieldId fqn, decode(x0.upperId, 'ROOT', lowerId, upperId) aid, x0.lowerId,  x0.lvl, x1.afieldId, x1.afieldNm name, x1.afieldExp explain, typecd, isMndt isMndt, examid, defaultValue
    from 
        (
        select upperid, lowerid, level lvl
        from afield_rel_tblc
        connect by prior lowerid = upperid
        start with upperid = 'ROOT'
        ) x0, afield_tblc x1
    where decode(x0.upperId, 'ROOT', lowerId, upperId) = x1.afieldId 


	user
	select '/datas/user/' || userid fqn, userid, usernm name, userid password
	from user_tblc where isUser = 'T' and to_char(sysdate, 'yyyymmdd') between enrolday and retireday
        
        