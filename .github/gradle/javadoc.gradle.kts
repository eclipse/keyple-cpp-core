tasks {
    withType<Javadoc> {
        val title = "${project.property("title")} - ${project.version}"

        (options as StandardJavadocDocletOptions).apply {
            windowTitle = title
            header = """<div style="margin-top: 7px">
                <a target="_parent" href="https://keyple.org/">
                    <img src="https://keyple.org/docs/api-reference/java-api/keyple-java-core/1.0.0/images/keyple.png" 
                        height="20px" style="background-color: white; padding: 3px; margin: 0 10px -7px 3px;"/>
                </a>
                $title
            </div>"""
            docTitle = title
            setUse(true)
            bottom = System.getenv("Copyright © Eclipse Foundation, Inc. All Rights Reserved.")
            overview = "src/main/javadoc/overview.html"
        }
    }
}