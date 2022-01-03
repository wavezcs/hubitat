metadata { 
    definition ( name: "CS Garage Door Updater", namespace: "cscott", author: "cscott", importUrl:"https://raw.githubusercontent.com/wavezcs/hubitat/main/GaragePost.groovy?token=AXDIDULC5LZXRCMVPC2Z7OLB2JTAE") {
        capability "Refresh"
        capability "PushableButton"
    }
}
//test
def fcm = "https://fcm.googleapis.com/fcm/send"
def fcm_header_key = "Authorization"
def fcm_header_value = "key=AIzaSyASngeHImHv_SHjRT-ygDNNo3FXxOOUdy8"
def apigw = "https://apigw.c-scott.com/sethistory"
def apigw_header_key = "x-api-key"
def apigw_header_value = "qlmZMOraDka0UE2zOLvXuyfUFsLAlQc6poGdXgWb"
    
def push(btnum) {
    //Button Pushes 10: Door 1 Open; 11 Door 1 Close
    //Button Pushes 20: Door 2 Open; 21 Door 2 Close
    log.debug "pushed button $btnum"

	def door
	def status

	switch (btnum) {
		case 10:
			door = 1
			status = "open"
			break;
		case 11:
			door = 1
			status = "closed"
			break;
		case 20:
			door = 2
			status = "open"
			break;
		case 21:
			door = 2
			status = "closed"
			break;
		default:
			return false
	}

	//FCM Post
	currentTime = getTime().toString()
	json = '{ "data": { "door": "g${door}", "status": "${status}", "time": "${currentTime}" }, "to" : "/topics/wg_data" }'
	sendAsynchttpPost (fcm, fcm_header_key, fcm_header_value, json, "fcm")

	//API-GW Post
	json = '{ "data": { "door": "g${door}", "status": "${status}", "time": "${currentTime}" }, "to" : "/topics/wg_data" }'
	sendAsynchttpPost (apigw + "hid=${door}&dtdbash=${currentTime}:${door}&daction=${status}", apigw_header_key, apigw_header_value, "", "apigw")

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
