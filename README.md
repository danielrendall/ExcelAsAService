# Excel as a Service

An Excel service to be deployed to the [Services-as-a-Service server](https://github.com/danielrendall/ServicesAsAService)

This service lets you create XML files representing spreadsheets via some
process which produces XML (in my original use case for this, this was an XProc
transformation being run with [XML Calabash](https://xmlcalabash.com/)), post
them to an endpoint with `curl` and have them transformed into Excel .xlsx files
which can then be saved.

**This is intended for use on a machine not accessible from the internet. The
server is built around the idea of remote code execution - it should not be made
available to scamps and scallywags who might do naughty things with it.**

## Usage

Sketchy initial documentation, to be improved:

Build this, using the sbt "assembly" task to create an "assembled" version of
the "plugin" JAR, then PUT it to the _service endpoint on your running server
like so:

```shell
curl -XPUT --data-binary @the_assembled_jar.jar http://localhost:1810/_service/excel
```

Create your XML, making sure that it conforms to the [schema](schema/src/main/xsd/excelv1.xsd)

POST your XML to the newly created endpoint and save the result

```shell
curl -XPOST --data-binary @some_xml.xml http://localhost:1810/excel > output.xlsx
```

Note - use the `--http1.0` flag in your `curl` to avoid irritating 1s delays

## Implementation notes

This is intended for creating incredibly simple Excel spreadsheets. As long as
the version is < 1.0.0, the schema may change slightly. My plan is that at some
point I will freeze the schema as "version 1" (the code written reflects this
intention) and then if I want to develop more complicated things, I'll define a
"version 2" schema which may or may not be backwards compatible.

Internally, the supplied XML is converted by some auto-generated parsing code
created with [scalaxb](https://scalaxb.org/) and then mapped to an
[internal model](plugin/src/main/scala/uk/co/danielrendall/saas/excel/model/model.scala)
from which the spreadsheet is generated. At the moment, the model is essentially
identical to the version 1 schema. The plan is that the model can be made more
complicated as necessary, but backward compatibility can be maintained by
ensuring we can always map the parsed version 1 output to this new improved
model.

## Status

WARNING - this is pretty much untested at the moment

* Needs proper documentation
* And also some actual tests
* Figure out how to make the "release" task only release the artifact generated
  by the "plugin" project
