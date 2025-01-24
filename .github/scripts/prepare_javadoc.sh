#!/bin/sh

repository_name=$1
version=$2
is_snapshot=$3

if [ "$is_snapshot" = true ]
then
 version="$version-SNAPSHOT"
fi
echo "Computed current API version: $version"
echo "Cloning repository: $repository_name..."
git clone https://github.com/eclipse-keyple/$repository_name.git
cd $repository_name

echo "Switching to 'doc' branch..."
git checkout -f doc

echo "Cleaning up existing SNAPSHOT directories..."
rm -rf *-SNAPSHOT

echo "Creating directory for version $version..."
mkdir -p $version

echo "Copying Javadoc files to $version..."
cp -rf ../build/docs/javadoc/* $version/

echo "Copying UML diagrams to $version..."
cp -rf ../src/main/uml/api_*.svg $version/

latest_stable=$(ls -d [0-9]*/ | grep -v SNAPSHOT | cut -f1 -d'/' | sort -Vr | head -n1)

echo "Latest stable version detected: $latest_stable"

if [ ! -z "$latest_stable" ]; then
   echo "Updating latest stable version directory..."
   rm -rf latest-stable
   mkdir -p latest-stable
   cp -rf "$latest_stable"/* latest-stable/
   echo "Creating robots.txt file..."
   cat > robots.txt << EOF
User-agent: *
Allow: /
Allow: /latest-stable/
Disallow: /*/[0-9]*/
EOF
fi

echo "Updating version list..."
sorted_dirs=$(ls -d [0-9]*/ | cut -f1 -d'/' | sort -Vr)

echo "| Version | Documents |" > list_versions.md
echo "|:---:|---|" >> list_versions.md

for directory in $sorted_dirs
do
 diagrams=""
 for diagram in `ls $directory/api_*.svg | cut -f2 -d'/'`
 do
   name=`echo "$diagram" | tr _ " " | cut -f1 -d'.' | sed -r 's/^api/API/g'`
   diagrams="$diagrams<br>[$name]($directory/$diagram)"
 done
 if [ "$directory" = "$latest_stable" ]; then
     echo "| **$directory (latest stable)** | [API documentation](latest-stable)$diagrams |" >> list_versions.md
 else
     echo "| $directory | [API documentation]($directory)$diagrams |" >> list_versions.md
 fi
done

echo "Computed all versions:"
cat list_versions.md

echo "Committing and pushing changes..."
git add -A
git config user.email "${repository_name}-bot@eclipse.org"
git config user.name "Eclipse Keyple Bot"
git commit --allow-empty -m "docs: update documentation for version $version"
git push origin doc

rm -rf ../$repository_name
echo "Documentation update completed."