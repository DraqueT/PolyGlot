#!/bin/bash
# Copies BETA version to relevant space to be uploaded via Google Drive

file_dest="/Users/draque/Google Drive/Permanent_Share/PolyGlot_BETA.zip"
file_orig="PolyGlot_BETA.zip"
beta_warn_dest="dist/BETA_WARNING.txt"
beta_warn_orig="BETA_WARNING.txt"
readme_remove="dist/README.txt"

if [ -f "$file_dest" ]
then
	rm "${readme_remove}"
	cp "${beta_warn_orig}" "${beta_warn_dest}" 
	echo -e "\n\nCompiled:" >> "${beta_warn_dest}"
	date >> "${beta_warn_dest}"
	cat >> "${beta_warn_dest}"
	echo Archiving PolyGlot Beta...
	zip -r PolyGlot_BETA.zip dist
	echo Copying to public share folder...
	mv "${file_orig}" "${file_dest}"
else
	echo "Expected folder structure mission. Beta not uploaded."
fi