import com.urbancode.air.AirPluginTool
import HttpHelper
import static groovyx.net.http.Method.*



/* This gets us the plugin tool helper. 
 * This assumes that args[0] is input props file and args[1] is output props file.
 * By default, this is true. If your plugin.xml looks like the example. 
 * Any arguments you wish to pass from the plugin.xml to this script that you don't want to 
 * pass through the step properties can be accessed using this argument syntax
 */
final airTool = new AirPluginTool(args[0], args[1])

/* Here we call getStepProperties() to get a Properties object that contains the step properties
 * provided by the user. 
 */
final def props = airTool.getStepProperties()

/* This is how you retrieve properties from the object. You provide the "name" attribute of the 
 * <property> element.
 * 
 */
final def rundeck_url = props['rundeck_url']
final def rundeck_authentication_token = props['rundeck_authentication_token']
final def rundeck_job_name = props['rundeck_job_name']
final def rundeck_job_parameters = props['rundeck_job_parameters']
final def final_rundeck_url = """${rundeck_url}/${rundeck_job_name}/run?authtoken=${rundeck_authentication_token}&${rundeck_job_parameters}&format=xml"""

//Using HttpHelper to execute rundeck job
HttpHelper helper = new HttpHelper(final_rundeck_url,POST,null) 
def response = helper.getResponse()
def rundeck_execution_url = new XmlSlurper().parseText(response).execution[0].@href.toString()

//Set an output property
airTool.setOutputProperty("response", "${response}")
airTool.setOutputProperty("rundeck_execution_url", "${rundeck_execution_url}")
airTool.storeOutputProperties()//write the output properties to the file

//To retrieve job status
def response_xml = new XmlSlurper().parseText(response)
def status_url = rundeck_execution_url+'/output?authtoken='+rundeck_authentication_token+'&format=xml'
def job_status = new HttpHelper(status_url).getResponse()
def job_status_xml = new XmlSlurper().parseText(job_status)
def current_status = job_status_xml.execState.toString()
def completed = job_status_xml.completed.toString()

def printStatus =	{ String msg_type, String msg	->
	println '\n############################################################\n'
	println "${msg_type}: ${msg}"
	println(new HttpHelper(status_url.replace('xml','text')).getResponse())
}

while (current_status=='running') {
	//Reset status
	job_status = new HttpHelper(status_url).getResponse()
	job_status_xml = new XmlSlurper().parseText(job_status)
	current_status = job_status_xml.execState.toString()
	completed = job_status_xml.completed.toString()

	switch(current_status) {
		case 'succeeded':
			println '...'
			printStatus.call('INFO','Job acionado e executado com sucesso')
			System.exit(0)
		case 'failed':
			printStatus.call('ERRO','Job falhou.')
			System.exit(1)
		case 'failed-with-retry':
			printStatus.call('ERRO','Job falhou.')
			System.exit(1)
		case 'aborted':
			printStatus.call('ERRO','Job abortado.')
			System.exit(1)
		case 'running':
			print '...'
			break
		default:
			printStatus.call('ERRO','Job com erro inesperado.')
			System.exit(1)
	}
	sleep(10000)
}
if (current_status!='runnning') {
	printStatus.call('ERRO','Job com erro inesperado.')
	System.exit(1)
}