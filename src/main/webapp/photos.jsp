<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="constants.ConfigConstants"%>
    <%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Photos</title>

<link rel="stylesheet" href="//blueimp.github.io/Gallery/css/blueimp-gallery.min.css">
<link rel="stylesheet" href="./css/bootstrap-image-gallery.min.css">

<link rel="stylesheet" href="./css/dropzone.css">
<style>
	.image-selection-corner {
		position : absolute;
		right : 2px;
		top : 2px;
		height : 40px;
		width : 40px;
		display:none;
	}
	.thumbnail:hover .image-selection-corner {
		display:block;
	}
	.selectbox {
		position : absolute;
		right : 10px;
		top : 10px;
		font-size : 22px !important;
		color : black;
	}
	.selected .selectbox {
		color : #5f9be4;
	}
	.selected img {
		transform: translateZ(0px) scale3d(0.82, 0.86, 1);
		border: solid #5f9be4 8px  !important;
	}
	
</style>
</head>
<body>
<%@ include file="header.jsp" %>
<!-- ********************************************
	Common JS imports: Start
 -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<!-- 
	Common JS imports: End
 ************************************************-->
<script src="./js/dropzone.js"></script>
<script src="./js/toast.min.js"></script>
<script type="text/javascript" src="./js/heartbeat.js"></script>
<script>
	function displayDropzone()
	{
		//document.getElementById("dropzone-area").style.display = 'block';
		cloudinary.openUploadWidget({
				cloud_name: 'welfare-cdn',
				upload_preset: '<%=ConfigConstants.get("cloudinary_upload_prest")%>',
				max_file_size:<%=ConfigConstants.get("cloudinary_max_file_size")%>,
			}, 
	        function(error, result) {
				if(result.length){
					var photosArr = [];
					result.forEach(function(p){
						var pivot = p.secure_url.indexOf('upload/');
						var thumbUrl = p.secure_url.substring(0, pivot+7).concat('c_limit,h_200/').concat(p.secure_url.substring(pivot+7));
						photosArr.push({'publicId' : p.public_id,
							 'url' : p.secure_url,
							 'thumbUrl' : thumbUrl,
							 'fileName' : p.original_filename,
							 'createdAt' : p.created_at});
					});
					var postData = {'from' : '<s:property value="from"/>',
							 'upPhotos' : JSON.stringify(photosArr)
					};
					<s:if test="%{from==\"event\"}">
						postData.eventId = <s:property value="eventId" />; 
					</s:if>
					$.ajax({
					    method: "POST",
					    url: "uploadPhotosAction",
					    data: postData,
					    traditional: true,
					    success:
					        function(jsonResponse) {
					    		jsonResponse.listOfPhotos.forEach(function(img){
					    			var image = {
						    				id : img.id,
						    				url : img.url,
						    				thumbUrl : img.thumbUrl
						    		};
						    		imageElement(image, <s:property value="#session.owner" />);
					    		});
								iqwerty.toast.Toast(jsonResponse.ajaxResponseDummyMsg);
								loader.stop();	
							}
					});
				}				
				
			});
	}
		
	
	function deleteSelected()
	{
		jQuery.ajaxSettings.traditional = true;
		if(selectedItemsArr.length) {
			loader.start();
			$.getJSON('ajaxDeletePhotoAction',
					{'deletePhotosIdArray' : selectedItemsArr.toString()},
					function(jsonResponse) {
							
						iqwerty.toast.Toast(jsonResponse.ajaxResponseDummyMsg);
						for(var i = 0; i<selectedItemsArr.length;i++)
							{
								$('#div'+selectedItemsArr[i])[0].remove();	
							}
							loader.stop();	
					});
		}
	}
	function addToSlideshow()
	{
		jQuery.ajaxSettings.traditional = true;
		if(selectedItemsArr.length){
			loader.start();
			$.getJSON('ajaxAddToCoverAction',
					{'addToSlideshow' : selectedItemsArr},
					function(jsonResponse) {
						loader.stop();
						iqwerty.toast.Toast(jsonResponse.ajaxResponseDummyMsg);
						 setTimeout(function(){
							   window.location.href = window.location.href;
						   },3000);
					}
					);
		}
		jQuery.ajaxSettings.traditional = false;
		
	}
	function removeFromSlideshow()
	{
		if(selectedItemsArr.length){
			loader.start();
			jQuery.ajaxSettings.traditional = true;
			$.getJSON('ajaxRemoveFromCoverAction',
					{'removeFromSlideshow' : selectedItemsArr},
					function(jsonResponse) {
						loader.stop();
						iqwerty.toast.Toast(jsonResponse.ajaxResponseDummyMsg);
						 setTimeout(function(){
							   window.location.href = window.location.href;
						   },3000);
					}
					);
			jQuery.ajaxSettings.traditional = false;
		}
		
	}
	
</script>
		<%@ include file="ngoHeader.jsp" %>
<hr>
		<div class="row">
			<div class="col-md-5 col-sm-12 col-xs-12">
				<h3 class="pageHeading"><s:property value="header" /></h3>
			</div>
			<div class="col-md-7 col-sm-12 col-cs-12 pull-right" style="padding-top:20px;">
				 <s:if test="%{#session.owner}">
					<button class="btn btn-simple"  name="addMore" onClick="displayDropzone()" title="Upload Photos">Add Photos</button>
					<s:if test="%{!album.equalsIgnoreCase(\"Slideshow\")}">
						<button class="btn btn-simple" disabled id="bAddToSlideshow" onClick="addToSlideshow()" title="Add to Cover">Add to Cover Photos</button>
					</s:if>
					<s:else>
						<button class="btn btn-simple" disabled id="bRemoveFromSlideshow" onClick="removeFromSlideshow()" title="Remove from Slideshow">Remove from Cover Photos</button>
					</s:else>
					<button class="btn btn-simple" disabled id="bDelete" onClick="deleteSelected()" title="Delete selected">Delete Selected</button>
				</s:if>
				<s:if test="%{from==\"ngo\"}">
			       		<div class="input-group-btn" style="float:right; width:initial;">
					        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
					        Album: <s:property value="album" /> <span class="caret"></span></button>
					        <ul class="dropdown-menu dropdown-menu-right">
					        	
					          <li><a href="<s:url action='getPhotos'>
							             	 <s:param name="from">ngo</s:param>
							             	 <s:param name="album">slideshow</s:param>
							             	 <s:param name="pageOwnerCode"><s:property value="pageOwnerCode"/></s:param>
							              </s:url>" title="Slideshow pics">Slideshow</a></li>
					          <li><a href="<s:url action='getPhotos'>
							             	 <s:param name="from">ngo</s:param>
							             	 <s:param name="album">all</s:param>
							             	 <s:param name="pageOwnerCode"><s:property value="pageOwnerCode"/></s:param>
							              </s:url>" title="View All" >All</a></li>
					        </ul>
					      </div>
			     </s:if>
			</div>
				 
	     </div>
			<script type="text/javascript" src="./js/jquery.ui.widget.js"></script>
			<script type="text/javascript" src="./js/jquery.iframe-transport.js"></script>
			<script type="text/javascript" src="./js/jquery.fileupload.js"></script>
			<script type="text/javascript" src="./js/jquery.cloudinary.js"></script> 
			<script src="//widget.cloudinary.com/global/all.js" type="text/javascript"></script>
		<hr>
		<div class="row">
		<script>
			var selectedItemsArr = [];
			var selectElement = function(event){
				var tgt = event.currentTarget;
				var thisId = tgt.getAttribute('data-img-id');
				thisId = parseInt(thisId);
				var chk = tgt.getElementsByClassName('selectbox')[0];
				if(selectedItemsArr.includes(thisId)){
					chk.classList.replace('fa-check-square-o', 'fa-square-o');
					selectedItemsArr.splice(selectedItemsArr.indexOf(thisId), 1);
					$('#div'+thisId).removeClass('selected');
				}
				else{
					chk.classList.replace('fa-square-o', 'fa-check-square-o');
					selectedItemsArr.push(thisId);
					$('#div'+thisId).addClass('selected');
				}
				checkSelectedItemsArr();
					
			}
			function checkSelectedItemsArr(){
				var selection = (selectedItemsArr && selectedItemsArr.length)
				$('#bDelete').attr('disabled', !selection);
				$('#bAddToSlideshow').attr('disabled', !selection);
				$('#bRemoveFromSlideshow').attr('disabled', !selection);
			}
			function imageElement(image, owner) {
				var imgId = image.id;
				var imgUrl = image.url;
				var thumbUrl = image.thumbUrl;
				var imgDiv = $('<div />', {
					'class' : 'col-md-3 col-sm-6 col-xs-12 thumbnail',
					'id' : 'div' + imgId
				});
				if (owner) {
					var selectionCorner = $('<div />', {
						'data-img-id' : imgId,
						'class' : 'image-selection-corner',
						'click' : selectElement
					}).appendTo(imgDiv);
					var checkIcon = $('<i />',{
						'class' : 'fa fa-square-o selectbox'
					}).appendTo(selectionCorner);
					
				}
				var link = $('<a />', {
					'href' : imgUrl,
					'data-gallery' : ''
				}).appendTo(imgDiv);
				var img = $(
						'<img />',
						{
							'style' : 'object-fit:cover ; height:200px; border-radius: 4px',
							'src' : thumbUrl
						}).appendTo(link);
	    	 
				var galleryDiv = $('.galleryDiv');
				imgDiv.appendTo(galleryDiv);
				scene.update(); // make sure the scene gets the new start position
				$("#loader").removeClass("active");
			}

			function getListOfImages(imgPageNo, ownerId, from, album) {

				loader.start();
				
				var data = {};
				data.imgPageNo = imgPageNo;
				if(from === 'event')
					data.eventId = ownerId;
				else
					data.pageOwnerCode = ownerId;
				data.from = from;
				data.album = album;
				$.getJSON('ajaxGetPhotos', data, function(jsonResponse) {
					loader.stop();
					var aboutUsArray = jsonResponse.listOfPhotos;
					aboutUsArray.forEach(function(item, index) {
						imageElement(item, <s:property value="#session.owner" />);
					});

					if (jsonResponse.endReached) {
						scene.remove();
						$("#loader").css('display', 'none');
					} 
				});
			}
		</script>
		<div class="dynamicContent">
			<div class="row galleryDiv">
			</div>
			<div id="loader" style="text-align: center;">
				<img src="./images/loading.gif">
			</div>
			<script src="//cdnjs.cloudflare.com/ajax/libs/ScrollMagic/2.0.5/ScrollMagic.min.js"></script>
			<script	src="//cdnjs.cloudflare.com/ajax/libs/ScrollMagic/2.0.5/plugins/debug.addIndicators.min.js"></script>
			<script>
				var controller = new ScrollMagic.Controller();
				var currPage = 0;
				// build scene
				var scene = new ScrollMagic.Scene({
					triggerElement : ".dynamicContent #loader",
					triggerHook : "onEnter"
				}).addTo(controller).on("enter",
						function(e) {
							if (!$("#loader").hasClass("active")) {
								$("#loader").addClass("active");
								if (console) {
									console.log("loading new items");
								}
								// simulate ajax call to add content using the function below
								<s:if test='%{from.equals("ngo")}'>
								getListOfImages(++currPage,	"<s:property value="pageOwnerCode" />",	"<s:property value="from" />", "<s:property value="album" />")
								</s:if>
								<s:if test='%{from.equals("event")}'>
									getListOfImages(++currPage,	"<s:property value="eventId" />",	"<s:property value="from" />", "<s:property value="album" />")
								</s:if>
							}
						});
			<s:if test='%{from.equals("ngo")}'>
				getListOfImages(0, "<s:property value="pageOwnerCode" />",	"<s:property value="from" />", "<s:property value="album" />")
			</s:if>
			<s:if test='%{from.equals("event")}'>
				getListOfImages(0,	"<s:property value="eventId" />",	"<s:property value="from" />", "<s:property value="album" />")
			</s:if>
			</script>
						</div>
		
	    	 	<!-- The Bootstrap Image Gallery lightbox, should be a child element of the document body -->
				<div id="blueimp-gallery" class="blueimp-gallery blueimp-gallery-controls" data-use-bootstrap-modal="false">
				    <!-- The container for the modal slides -->
				    <div class="slides"></div>
				    <!-- Controls for the borderless lightbox -->
				    <h3 class="title"></h3>
				  	<a class="prev"><i class="fa fa-chevron-left"></i></a>
				    <a class="next"><i class="fa fa-chevron-right"></i></a>
				    <a class="close"><i class="fa fa-close"></i></a>
				    <a class="play-pause"></a>
				    <ol class="indicator"></ol>
				    <!-- The modal dialog, which will be used to wrap the lightbox content -->
				    <div class="modal fade">
				        <div class="modal-dialog">
				            <div class="modal-content">
				                <div class="modal-header">
				                    <button type="button" class="close" aria-hidden="true">&times;</button>
				                    <h4 class="modal-title"></h4>
				                </div>
				                <div class="modal-body next"></div>
				                <div class="modal-footer">
				                    <button type="button" class="btn btn-default pull-left prev">
				                        <i class="fa fa-chevron-left"></i>
				                        Previous
				                    </button>
				                    <button type="button" class="btn btn-primary next">
				                        Next
				                        <i class="fa fa-chevron-left"></i>
				                    </button>
				                </div>
				            </div>
				        </div>
				    </div>
				</div>
	    	  </div>
   	  	<script src="//blueimp.github.io/Gallery/js/jquery.blueimp-gallery.min.js"></script>
		
	</div>
	<%@ include file="footer.jsp" %>
</body>
</html>