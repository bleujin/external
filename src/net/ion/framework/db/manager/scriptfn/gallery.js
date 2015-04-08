new function(){
	importPackage(java.lang)
	importPackage(net.ion.craken.node)
	
	this.createWith = function(mirror, v_galId, v_galCatId, v_fileNm, v_fileSize, v_width, v_height, v_typeCd, v_userId){
		mirror.gallery(v_galId, v_userId) ;
		System.out.println(v_galId) ;
	}, 
	
	this.updateFileMetaWith = function(mirror, v_galId, v_fileNm, v_subject, v_content, v_fileMeta, v_userId){
		mirror.galleryInfo(v_galId, v_fileNm, v_subject, v_content, v_fileMeta, v_userId) ;
	}, 
	
	this.updateWith = function (mirror, v_galId, v_fileNm, v_fileSize, v_width, v_height, v_typeCd, v_userId) {
		mirror.gallery(v_galId, v_userId) ;
	}, 
	
	this.removeWith = function(mirror, v_galIds) {
		mirror.galleryClear(v_galIds) ;
	}
	
}