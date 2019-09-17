import com.urbancode.air.AirPluginTool
import HttpHelper


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
HttpHelper helper = new HttpHelper(final_rundeck_url,"POST")
def responseText = helper.getResponseText()

//Set an output property
airTool.setOutputProperty("responseText", "${responseText}")
airTool.storeOutputProperties()//write the output properties to the file

//Retrieve job status
def response = new XmlParser().parseText(responseText)
def status_url = response.execution[0].@href+'/output?authtoken='+rundeck_authentication_token
def job_status = new HttpHelper(status_url).getResponseText()
def current_status_data = new XmlParser().parseText(job_status)
def current_status = current_status_data.execState.text()
def completed = current_status_data.completed.text()

while (current_status=='running') {
	//Reset status
	job_status = new HttpHelper(status_url).getResponseText()
	current_status_data = new XmlParser().parseText(job_status)
	current_status = current_status_data.execState.text()
	completed = current_status_data.completed.text()

	switch(current_status) {
		case 'succeeded':
			println '...'
			println 'Deploy efetuado com com sucesso'
			System.exit(0)
		case 'failed':
			println '\n##################### DEPLOY FALHOU #####################\n'
			println(new HttpHelper(status_url.plus('&format=text')).getResponseText())
			System.exit(1)
		case 'aborted':
			println '\n##################### DEPLOY ABORTADO #####################\n'
			println(new HttpHelper(status_url.plus('&format=text')).getResponseText())
			System.exit(1)
		default:
			print '...'
	}
	sleep(10000)
}



