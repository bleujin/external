new function(){
	
	importPackage(java.lang)
	
	this.insertwith = function(mirror, _a, _b){
		System.out.println('Hello') ;
		
		session.tran(function(wsession) {
			wsession.pathBy("/sample/" + _a).property("a", _a).property("b", _b);
		}) ;
	}, 
	
	
	this.selectBY = function(mirror, _a){
		return session.ghostBy('/sample/'+ _a).toRows("a, b") ;
	}, 
	
	this.addbatchwith = function(mirror, _a, _b){
		session.tran(function(wsession){
			wsession.pathBy("/sample/" + _a).property("a", _a).property("b", _b) ;
		}) ;
	}, 
	
	this.delbatchwith = function(mirror, _a) {
		
		
		session.tran(function(wsession){
			wsession.pathBy("/sample/" + _a).removeSelf() ;
		}) ;
	}
}