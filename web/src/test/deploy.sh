#!/bin/bash
base=$(dirname $0)/../main/webapp
cp -r $base/addon $HOME/soffid/test/iam-console-3/work/soffid/iam-ear.ear/iam-web-*/
cp -r $(dirname $0)/../../target/classes/* $HOME/soffid/test/iam-console-3/work/soffid/iam-ear.ear/iam-web-*/WEB-INF/classes
cp -r $(dirname $0)/../main/resources/* $HOME/soffid/test/iam-console-3/work/soffid/iam-ear.ear/iam-web-*/WEB-INF/classes
