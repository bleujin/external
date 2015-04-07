new function(){
	importPackage(java.lang)
	
	this.fetchArticleWith = function(mirror, v_scatId, v_acatId, v_operDay, v_expireDay, v_priority, v_useFlg, v_artId){
		mirror.article(v_scatId, v_artId) ;
	}
}

