metadata { 
    definition ( name: "CS Garage Door Updater", namespace: "cscott", author: "cscott", importUrl:"https://raw.githubusercontent.com/wavezcs/hubitat/main/GaragePost.groovy?token=AXDIDULC5LZXRCMVPC2Z7OLB2JTAE") {
        capability "Refresh"
        capability "PushableButton"
    }
}

def fcm = "https://fcm.googleapis.com/fcm/send"
def fcm_header_key = "Authorization"
def fcm_header_value = "key=AIzaSyASngeHImHv_SHjRT-ygDNNo3FXxOOUdy8"
def apigw = "https://apigw.c-scott.com/sethistory"
def apigw_header_key = "x-api-key"
def apigw_header_value = "qlmZMOraDka0UE2zOLvXuyfUFsLAlQc6poGdXgWb"
    
//curl --header 'x-api-key: qlmZMOraDka0UE2zOLvXuyfUFsLAlQc6poGdXgWb' -k 'https://apigw.c-scott.com/sethistory?hid=1&dtdhash=" .. currentTime .. ":1&daction=Open'"
//'{ "data": { "door": "g1", "status": "Open", "time": "1641150303" }, "to" : "/topics/wg_data" }'

def push(btnum) {
    //Button Pushes 10: Door 1 Open; 11 Door 1 Close
    //Button Pushes 20: Door 2 Open; 21 Door 2 Close
    log.debug "pushed button $btnum"
	
	switch ()
}

def refresh() { ( sendAsynchttpPost( url, header_key, header_value, json, ctype) ) }

def sendAsynchttpPost(url, authkey, authvalue, payload) {
    def postParams = [
		uri: url,
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: [header_key: header_value],
		body : json
	]

	asynchttpPost('postcallback', postParams, [calltype: ctype])
}

def postcallback(response, data) {
    if(data["calltype"] == "fcm")
    	log.debug "fcm data was passed successfully"
    
    log.debug "Async Post Status: ${response.status} (${response.data})"
}