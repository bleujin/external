new function(){
	importPackage(java.lang)
	importPackage(net.ion.framework.util)

	
	this.createWith = function(mirror, v_artId, v_catId) {
		mirror.content(v_catId, v_artId) ;
	}, 
	

	this.rewriteWith = function(mirror, v_catId, v_artId){
		mirror.content(v_catId, v_artId) ;
	}, 
	
	this.rewriteAndApproveWith = function(mirror, v_catId, v_artId){
		mirror.content(v_catId, v_artId) ;
	},
	
	this.rewriteAndReapproveWith = function(mirror, v_catId, v_artId, v_modSerNo, v_artSubject, v_artCont, v_signUserId, v_isSign, v_thumbImgName, v_operDay, v_expireDay, v_keywords){
		mirror.content(v_catId, v_artId) ;
	}, 
	
	this.removeWith = function(mirror, v_catId, v_artId){
		mirror.content(v_catId, v_artId) ;
	}, 
	
	this.recoverWith = function(mirror, v_artId){
		mirror.content(v_artId) ;
	}, 
	
	this.clearWith = function(mirror, v_artId){
		mirror.contentClear(v_artId) ;
	}
	
}