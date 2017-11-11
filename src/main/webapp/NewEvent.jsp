<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="s" uri="/struts-tags" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><s:if test="%{mode=='create'}">Create Event :</s:if><s:else>Edit Event :</s:else> <s:property value="#session.username" /></title>
</head>
<body>

<%@ include file="header.jsp" %>

<!-- Begin of Main Content HTML -->
		

<!-- ********************************************
	Common JS imports: Start
 -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
	<script src="./js/validator.js"></script>
	<script type="text/javascript" src="./js/heartbeat.js"></script>
<!-- 
	Common JS imports: End
 ************************************************-->
 <script>
 $(document).ready(function() {
		
		$('#state').change(function(event) {
			var state = $("select#state").val().toString();
			loader.start();
			$.getJSON('ajaxStateChangeAction',
					{selectedState : state},
					function(jsonResponse) {
						loader.stop();
						$('#ajaxResponse').text(jsonResponse.dummyMsg);
						var select = $('#city');
						select.find('option').remove();
						$.each(jsonResponse.cityList,
								function(key,value) {
									$('<option>').val(value).text(value).appendTo(select);
								});
						$( "#city" ).trigger( "change" );
					});
			});
		$('#city').change(function(event) {
			loader.start();
			var city = $("select#city").val().toString();
			$.getJSON('ajaxCityChangeAction', {selectedCity : city}, function(jsonResponse) {
				loader.stop();
				$('#ajaxResponse').text(jsonResponse.dummyMsg);
				var select = $('#pincode');
				select.find('option').remove();
				$.each(jsonResponse.pincodeList, function(key,value) {$('<option>').val(value).text(value).appendTo(select);
				});
				$( "#pincode" ).trigger( "change" );
			});
		});
		$('#pincode').change(function(event) {
			loader.start();
			var pincode = $("select#pincode").val().toString();
			$.getJSON('ajaxPincodeChangeAction', {selectedPincode : pincode}, 
				function(jsonResponse) {
					loader.stop();
					$('#ajaxResponse').text(jsonResponse.dummyMsg);
					var select = $('#area');
					select.find('option').remove();
					$.each(jsonResponse.areaList, function(key,value) {$('<option>').val(value).text(value).appendTo(select);
					});
				});
		});
		
		
	});
 </script>
 
 <%@ include file="ngoHeader.jsp" %>
 <hr>
 <div class="row">
	<div class="col-md-3 ">
		<h3 class="pageHeading"><s:property value="heading"/></h3>
	</div>
</div>
<hr></hr>
	<div class="row">
		<form action=<s:property value='newEventFormAction' /> enctype="multipart/form-data" method="post" data-toggle="validator">
		<s:hidden name="eventBean.id" value="%{eventBean.id}" />
		<s:if test="hasActionErrors()">
		                <s:actionerror/>
		                <p style="color:red" id="pActionMessage"></p>
		                    <script>
		                    	document.getElementById("pActionMessage").innerHTML = document.getElementsByClassName("errorMessage")[0].childNodes[1].childNodes[0].innerHTML;
		                    </script>
		                 
		                </s:if>
		<div class="col-md-5">
			<div class="row">
			  <div class="form-group col-md-8">
			    <label for="name">Name</label>
			    <s:textfield cssClass="form-control" id="name" placeholder="Name" name="eventBean.name" value="%{eventBean.name}" 
			    required="" data-error="Required Field!!" maxlength="100"></s:textfield>
			    <div class="help-block with-errors"></div>
			  </div>
			 </div>
			 <div class="row">
				<div class="col-md-6 col-xs-6 form-group">
					<label for="element_9_5">State</label>
					<s:select list="stateList" id="state" name="eventBean.addressBean.state" cssClass="form-control" value="%{eventBean.addressBean.state}"
					required="" data-error="Required Field!"></s:select>
					<div class="help-block with-errors"></div>
				</div>
				<div class="col-md-6 col-xs-6 form-group">
					<label for="element_9_6">City</label>
					<s:select list="cityList" cssClass="form-control" id="city" name="eventBean.addressBean.city" value="%{eventBean.addressBean.city}"
					required="" data-error="Required Field!"></s:select>
					<div class="help-block with-errors"></div>
				</div>
			</div> 
			<div class="row">
				<div class="col-md-6 col-xs-6 form-group">
					<label for="element_9_4">Pincode</label>
					<s:select list="pincodeList" id="pincode" name="eventBean.addressBean.pincode" cssClass="form-control"
					value="%{eventBean.addressBean.pincode}" required="" data-error="Required Field!"></s:select>
					<div class="help-block with-errors"></div>
				</div>
				<div class="col-md-6 col-xs-6 form-group">
					<label for="element_9_7">Area</label>
					<s:select list="areaList" cssClass="form-control" id="area" name="eventBean.addressBean.area"
					value="%{eventBean.addressBean.area}" required="" data-error="Required Field!"></s:select>
					<div class="help-block with-errors"></div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-6 col-xs-6 form-group">
					<label for="element_9_1">Street/Colony/PlotNo.</label>
					 <s:textfield id="street" name="eventBean.addressBean.street" cssClass="form-control" value="%{eventBean.addressBean.street}" 
					 required="" data-error="Required Field!"> </s:textfield>
					 <div class="help-block with-errors"></div>
				</div>
			</div>
			 <div class="row"> 
			 	<label>When</label>
			 </div>
			 <div class="row"> 
			  <div class="form-group col-md-5">
			    Date- (MM/DD/YYYY)<input type=date class="form-control" id="date" name="eventBean.calendar" value="<s:property value='editEventDate' />" 
			    required="" data-error="Required Field!!" />
			    <div class="help-block with-errors"></div>
			  </div>
			   <div class="form-group col-md-5">
			  	Time- (HH:MM AM/PM)<s:textfield cssClass="form-control" id="time" name="eventBean.evtTime" value="%{eventBean.evtTime}" 
			  	required="" data-error="Wrong Format!" pattern="^[0-9][0-9]:[0-9][0-9] (A|P|a|p)(M|m)" maxlength="8" data-minlength="8"
			  	placeholder="11:30 AM"/>
			  	<div class="help-block with-errors"></div>
			  </div>
			 </div>
			<div class="row"> 
			  <div class="form-group col-md-12">
			  	<label for="element_6">Photo </label>
				<s:if test="%{mode=='create'}">
					<s:file id="element_6" name="imgFile" cssClass="form-control" required='' 
					 data-filesize="1048576" data-img-file-validator = "imgFileValidator">
					</s:file>
					<div class="help-block with-errors"></div>
				</s:if>
				<s:else>
					<s:file id="element_6" name="imgFile" cssClass="form-control" data-filesize="1048576" data-img-file-validator = "imgFileValidator">
					</s:file>
					<div class="help-block with-errors">Max file size permitted is 1MB (.gif /.jpeg /.png /.bmp)</div>
				</s:else>
			  </div>
			 </div>
			 <br>
			 <div class="row">  
			  <div class="form-group">
			  	<s:submit cssClass="btn btn-primary" name="submitButton" ></s:submit>
			  </div>
			 </div>
		  </div>
		  <div class="form-group col-md-6">
		  	<label for="details">Details</label>
		    <s:textarea cssClass="form-control" rows="10" id="details" placeholder="About the event.." name="eventBean.details" 
		    value="%{eventBean.details}" required="" data-minlength="50" maxlength="1000" data-error="Minimum 50 characters required!!"></s:textarea>
		    <div class="help-block with-errors"></div>
		  </div>
		  <s:token />
		</form>
					<script>
							$(document).ready(function() {
								$('form').validator({
								 custom: {
									  imgFileValidator : function($el){
											  var result = false;
										  		if($el[0].value){
									  			 	var fileSizeMax = $el.data('filesize');
												    var fileSizeCheck = $el[0].files[0].size>fileSizeMax;
												    var fileTypeCheck = $el[0].files[0].type.startsWith("image");
									  					if ($el[0].files[0] && (fileSizeCheck || !fileTypeCheck)) {
									  						result = "Max file size permitted is 2MB (.gif /.jpeg /.png /.bmp)";
													    } else 
													    	result = false;
										  					
										  		}
										  		return result;
										  }
									  }
								});
							});
						</script>
	    </div>
	</div>
</div>
	<%@ include file="footer.jsp" %>
</body>
</html>
