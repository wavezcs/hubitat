definition(
	name: "NFC Manager",
	namespace: "cscott.nfc",
	author: "Chris Scott",
	description: "NFC Reader Tag Management",
	category: "Convenience",
	iconUrl: "",
	iconX2Url: "",
	singleInstance: true
)

//test
preferences {
	page(name: "mainPage")
}

def installed() {
	log.debug("App Installed")
	initialize()
}

def updated() {
	log.debug("Updated")
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug("Init")

	def nfcDevice = getChildDevice(getHexIP())
	if(!nfcDevice) {
		log.debug("Creating child device")
		nfcDevice = addChildDevice("cscott", "NFC Key Device", getHexIP(), null, [label: thisName, name: thisName])
	}
	subscribe(nfcDevice, "tag", eventHandler)
}

def mainPage() {
	if(!state.nfcNodes) { state.nfcNodes = [ "default": [name: "default", enabled: false]] }
	
	dynamicPage(name: "mainPage", title: "Configure NFC Manager", uninstall: true, install: true) {
		section {
			input("ip", "string", title:"IP Address", description: "Arduino IP", defaultValue: "192.168.0.102" ,required: true, displayDuringSetup: true)
			paragraph displayTable()
			input "logging", "bool", title: "Enable Logging?", defaultValue: true, submitOnChange: true
		}
	}
}

String displayTable() {
	String str = "<script src='https://code.iconify.design/iconify-icon/1.0.0/iconify-icon.min.js'></script>"
	str += "<style>.mdl-data-table tbody tr:hover{background-color:inherit} .tstat-col td,.tstat-col th { padding:8px 8px;text-align:center;font-size:12px} .tstat-col td {font-size:15px }" +
		"</style><div style='overflow-x:auto'><table class='mdl-data-table tstat-col' style=';border:2px solid black'>" +
		"<thead><tr style='border-bottom:2px solid black'><th style='border-right:2px solid black'>GUID</th><th style='border-right:2px solid black'>Name</th><th style='border-right:2px solid black'>Enabled</th><th style='border-right:2px solid black'>Action</th></tr></thead>"
	String X = "<i class='he-checkbox-checked'></i>"
	String O = "<i class='he-checkbox-unchecked'></i>"
	state.nfcNodes.each {node, nodeattr->
		log.debug("Loaded Nodes: $node")
		str += "<tr><td>$node</td><td>$nodeattr.name</td>"
		str += "<td>${buttonLink("toggle:$node",(nodeattr.enabled ? X : O))}</td>"
		str += "<td>${buttonLink("delete:$node","<iconify-icon icon='material-symbols-light:delete-outline'></iconify-icon>","black", "20px")}</td></tr>"
	}
	str += "</table></div>"
	
	def nfcDevice = getChildDevice(getHexIP())
	if(!nfcDevice) { 
		str += "<br><p><span style='color:black'>Refresh for child device link</span></p>"
	} else {
		str += "<br><p><a href='/device/edit/$nfcDevice.id' target='_blank' title='Open Device Page for $nfcDevice'>Open Child Device - $nfcDevice<span style='color:black'></span></p>"
	}
	
	str
}

String buttonLink(String btnName, String linkText, color = "#1A77C9", font = "15px") {
	"<div class='form-group'><input type='hidden' name='${btnName}.type' value='button'></div><div><div class='submitOnChange' onclick='buttonClick(this)' style='color:$color;cursor:pointer;font-size:$font'>$linkText</div></div><input type='hidden' name='settings[$btnName]' value=''>"
}

String getHexIP() {
    return settings.ip.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
}

void appButtonHandler(btn) {
	List action = btn.tokenize(":")
	if(action[0] == "delete") state.nfcNodes.remove(action[1])
	if(action[0] == "toggle") {
		def sb = state.nfcNodes[action[1]].enabled
		state.nfcNodes[action[1]].enabled = !sb
	}
}

def eventHandler(event) {

	if (event.name == "tag") {
		log.debug("Handling tag event: ${event.value}")
		def sTag = event.value.split(";")
		def tagDevice = sTag[0]
		def tagGUID = sTag[1]
		def nfcDevice = getChildDevice(getHexIP())

		if(state.nfcNodes.containsKey(tagGUID)) {

			if(state.nfcNodes[tagGUID].enabled) {
				log.debug("enabled device activating: $tagGUID")	
				nfcDevice.activatePush()	
			} else {
				log.debug("Ignoring disabled device: $tagGUID")	
			}
			
		} else {
			log.debug("Adding previously unknown device: $tagDevice; $tagGUID")
			state.nfcNodes.put(tagGUID, [name: "$tagDevice", enabled: false])
			nfcDevice.failedAuth(tagDevice)
		}
	}
}
