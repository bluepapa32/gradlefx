package gradlefx

import static groovyx.javafx.GroovyFX.start

start {
    stage(title: 'GroovyFX & Gradle Sample', visible: true) {
        scene(width: 840, height: 600) {
            fxml this.class.getResource('/gradlefx.fxml')
        }
    }
}
