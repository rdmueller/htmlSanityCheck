docker run --rm --entrypoint /bin/bash  -v ${PWD}:/project rdmueller/doctoolchain:v1.1.0 \
-c "doctoolchain . $1 $2 $3 $4 $5 $6 $7 $8 $9 -PmainConfigFile=config/docToolchain.groovy && exit"


