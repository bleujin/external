new function(){
	importPackage(java.lang)
	importPackage(net.ion.framework.util)

	
	this.approveWith = function(mirror, v_artId, v_catId) {
		mirror.content(v_catId, v_artId) ;
	}, 
	
	this.setActiveVersionWith = function(mirror, v_artId){
		mirror.content(v_artId) ;
	}, 
	
	this.copyWith = function(mirror, v_catId, v_artId) {
		throw new IllegalArgumentException("call bleujin") ;
	}, 
	
	this.moveWith = function(mirror, v_catId, v_artId) {
		mirror.contentClear(v_artId) ;
		mirror.content(v_artId) ;
	}
}