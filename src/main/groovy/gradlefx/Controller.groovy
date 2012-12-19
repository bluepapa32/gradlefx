package gradlefx

import javafx.application.*
import javafx.concurrent.*
import javafx.event.*
import javafx.fxml.*
import javafx.scene.control.*
import javafx.stage.*
import javafx.stage.FileChooser.ExtensionFilter

class Controller {

    @FXML
    ComboBox comboBox

    @FXML
    Button button

    @FXML
    TextArea console

    def projects = [ 'hellofx', 'groovyfx' ]

    def task;

    def service = [ createTask: { ->

        task = [ call: { ->

            try {

                def index = comboBox.selectionModel.selectedIndex
                if (index < 0) return

                def message = ''
                task.updateMessage(message)

                def project = projects[index]

                def destDir = new File(System.properties['java.io.tmpdir'], 'bluepapa32')
                if (!destDir.exists()) destDir.mkdirs()

                def projectDir = new File(destDir, "${project}")
                if (!projectDir.exists()) {

                    def zipFile = new File(destDir, "${project}.zip");
                    if (!zipFile.exists()) {

                        def url = new URL("https://github.com/bluepapa32/${project}/archive/master.zip")
                        message += "Downloading ${url}...\n\n"
                        task.updateMessage(message)

                        zipFile << url.newInputStream()
                    }

                    message += "Unzip ${zipFile}...\n\n"
                    task.updateMessage(message)
                    new AntBuilder().unzip (src: zipFile, dest: destDir)

                    new File(destDir, "${project}-master").renameTo(projectDir)
                }

                def buildFile  = new File(projectDir, 'build.gradle')
                def gradlew    = new File(projectDir, 'gradlew')

                def command = System.properties['os.name'].startsWith('Windows') ? [ 'cmd', '/c', gradlew, '-b', buildFile ] : [ 'sh', gradlew, '-b', buildFile ]
                def proc    = command.execute()
 
                message += "${command.join(' ')}\n"
                task.updateMessage(message)
                proc.in.eachLine { line -> task.updateMessage(message += "\n${line}") }
            } catch (e) {
                new StringWriter().withWriter { w ->
                    e.printStackTrace(w);
                    message += "${w}"
                    task.updateMessage(message)
                }
            }

        } ] as Task

    } ] as Service

    def onButtonClicked(ActionEvent e) {

        if (service.isRunning()) return

        button.disableProperty().bind(service.runningProperty())
        console.textProperty().bind(service.messageProperty())

        service.restart()
    }
}
