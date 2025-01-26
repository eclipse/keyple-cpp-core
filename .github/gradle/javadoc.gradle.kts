tasks {
    withType<Javadoc> {
        (options as StandardJavadocDocletOptions).apply {
            windowTitle = project.title + " - " + project.version
            header = """<div style="margin-top: 7px">
                <a target="_parent" href="https://keyple.org/">
                    <img src="https://keyple.org/docs/api-reference/java-api/keyple-java-core/1.0.0/images/keyple.png" 
                        height="20px" style="background-color: white; padding: 3px; margin: 0 10px -7px 3px;"/>
                </a>
                ${project.title} - ${project.version}
            </div>"""
            docTitle = "${project.title} - ${project.version}"
            setUse(true)
            bottom = System.getenv("Copyright © Eclipse Foundation, Inc. All Rights Reserved.")
            overview = "src/main/javadoc/overview.html"
        }
    }
}