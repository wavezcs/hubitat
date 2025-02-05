metadata {
	definition(name: "NFC Key Device", namespace: "cscott", author: "cscott") {
		capability "Momentary"
        capability "Actuator"
        attribute "LastBoot", "String"
        attribute "Device", "String"
        attribute "GUID", "String"
        attribute "LastScan", "String"
        attribute "LastPushed", "String"
        attribute "FailedAuth", "String"
	}

preferences {
	section("Config") {
		//input("ip", "string", title:"IP Address", description: "Arduino IP", defaultValue: "192.168.0.102" ,required: true, displayDuringSetup: true)
	}
}

}
            

def parse(String description) {
	def msg = parseLanMessage(description)
    log.debug "msg: ${msg}"
    
        if(msg.body == "boot") {
    		sendEvent(name: "LastBoot", value: nowFormatted())
        } else {
	        def tag = msg.body.split(":")
    	    if (tag[0] == "tag") {
                sendEvent(name: "tag", value: tag[1])
        	    def dID = tag[1].split(";")
                	sendEvent(name: "Device", value: dID[0])
                	sendEvent(name: "GUID", value: dID[1])
                	sendEvent(name: "LastScan", value: nowFormatted())
  		    }    
        }
}

public activatePush() {
	sendEvent(name: "momentary", value: "push")
	sendEvent(name: "LastPushed", value: nowFormatted())
}

public failedAuth(String tag) {
    sendEvent(name: "FailedAuth", value: tag)
}

String nowFormatted() {
  if(location.timeZone) return new Date().format('MMM-dd h:mm:ss a', location.timeZone)
  else                  return new Date().format('MMM-dd h:mm:ss a')
}