#!/bin/bash

# each line means a set of ids of the same anchor
# the last id is usually the most descriptive and should be used
# the others should be removed
ids=$(grep -h -o -r './' --include=\*.adoc --exclude-dir=target -e '^\(\[\[[0-9A-Za-z_\\-]\+\]\]\)\+' | tr -d "[" | tr -s "]]" ",")
redundantIds="";
for line in ${ids} ; do
	IFS=','
	labels=($line);
	len=${#labels[@]};
	if [[ $len != 1 ]] 
	then
		correctId=${labels[$len-1]};
		maxIncorrectIdIndex=$(($len-2));
#		echo "Max=$maxIncorrectIdIndex"
#		echo "Correct id=${correctId}";
		unset IFS;
		for i in $(seq 0 $maxIncorrectIdIndex) ; do
#			echo "Index: $i";
			redundantId=${labels[$i]};
			if [[ "$redundantIds" == *",$redundantId,"* ]]; then
				echo "Duplicit id must be fixed first: ${redundantId}";
				grep -o -r './' --include=\*.adoc --exclude-dir=target -e '^\(\[\[[0-9A-Za-z_\\-]\+\]\]\)\+' | grep "\[${redundantId}\]";
				exit 1;
			fi
			redundantIds="${redundantIds},${redundantId},"
			echo "Replacing $redundantId by $correctId and removing [[$redundantId]] labels...";
			find . -type f -name '*.adoc' ! -wholename '*/target/*' -exec sed -i -- "s/#${redundantId}/#${correctId}/g" {} +;
			find . -type f -name '*.adoc' ! -wholename '*/target/*' -exec sed -i -- "s/\[\[${redundantId}\]\]//g" {} +;
		done;
#	else 
#		echo "$labels is ok";
	fi;
done;

echo "Replacing link references by xref where it is possible.";
find . -type f -name '*.adoc' ! -wholename '*/target/*' -exec sed -i -r -- 's/link:([a-zA-Z0-9\-]+)\.html#/xref:\1.adoc#/g' {} +;
find . -type f -name '*.adoc' ! -wholename '*/target/*' -exec sed -i -r -- 's/link:#/xref:#/g' {} +;


