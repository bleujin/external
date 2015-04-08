new function(){
	importPackage(java.lang)
	
	this.updatewith = function(mirror, v_catId, v_artId){
		mirror.article(v_catId, v_artId) ;
	}, 
	
	this.removeWith = function(mirror, v_catId, v_artId) {
		mirror.articleClear(v_catId, v_artId) ;
	}, 
	
	this.copyWith = function(mirror, v_catId, v_targetCatId, v_artId) {
		mirror.article(v_targetCatId, v_artId) ;
	} , 
	this.moveWith = function(mirror, v_catId, v_targetCatId, v_artId) {
		mirror.articleClear(v_catId, v_artId) ;
		mirror.article(v_targetCatId, v_artId) ;
	}, 
	
	this.useWith = function(mirror, v_scatId, v_artId) {
		mirror.articleOnly(v_scatId, v_artId) ;
	}, 
	
	this.notUseWith = function(mirror, v_scatId, v_artId) {
		mirror.articleClear(v_scatId, v_artId) ;
	}, 
	
	// PROCEDURE relationWith(v_catId IN varchar2, v_artId IN number, v_relCatId IN varchar2, v_relArtId IN number) ; 
	// FUNCTION  relationRemoveWith(v_catId IN varchar2, v_artId IN number)  return Number; 
	
	this.setAllOperDayWith = function(mirror, v_artId, v_operDay, v_expireDay) {
		throw new IllegalArgumentException("call bleujin") ;
	}, 
	
	this.setOrderWith = function(mirror, v_catId, v_pArtId, v_orderNo) {
		throw new IllegalArgumentException("call bleujin") ;
	}
}