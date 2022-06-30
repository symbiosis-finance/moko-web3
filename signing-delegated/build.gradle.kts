plugins {
    id("multiplatform-library-convention")
    id("publication-convention")
}

dependencies {
    commonMainApi(projects.web3)
}
