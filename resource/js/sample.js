new function(){
	
	this.insertwith = function(_a, _b){
		
		session.tran(function(wsession) {
			wsession.pathBy("/sample/" + _a).property("a", _a).property("b", _b);
		}) ;
	}, 
	
	
	this.selectBY = function(_a){
		return session.ghostBy('/sample/'+ _a).toRows("a, b") ;
	}, 
	
	
	this.addbatchwith = function(_a, _b){
		session.tran(function(wsession){
			wsession.pathBy("/sample/" + _a).property("a", _a).property("b", _b) ;
		}) ;
	}, 
	
	this.delbatchwith = function(_a) {
		session.tran(function(wsession){
			wsession.pathBy("/sample/" + _a).removeSelf() ;
		}) ;
	}
}