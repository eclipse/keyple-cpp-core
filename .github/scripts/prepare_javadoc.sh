#!/bin/sh
set -e  # Exit immediately if any command fails

# Validate input arguments
if [ -z "$1" ] || [ -z "$2" ]; then
  echo "❌ Error: Usage: $0 <repository_name> <version>"
  exit 1
fi

repository_name=$1
version=$2

echo "🔍 Detected API version: $version"
echo "🔄 Cloning repository: $repository_name..."

# Optimized repository cloning
git clone --depth=1 https://github.com/eclipse-keyple/$repository_name.git
cd $repository_name

# Check if 'doc' branch exists before switching
if git show-ref --verify --quiet refs/heads/doc; then
  git checkout -f doc
else
  echo "⚠️ The 'doc' branch does not exist. Creating it..."
  git checkout -b doc
fi

# Cleanup previous SNAPSHOT versions
echo "🗑️ Removing old SNAPSHOT directories..."
rm -rf *-SNAPSHOT

# Create directory for the new version
echo "📂 Creating documentation folder for version $version..."
mkdir -p $version

# Copy Javadoc files if they exist
if [ -d "../build/docs/javadoc" ]; then
  cp -rf ../build/docs/javadoc/* $version/
else
  echo "⚠️ Warning: No Javadoc found."
fi

# Copy UML diagrams if they exist
if [ -d "../src/main/uml" ]; then
  cp -rf ../src/main/uml/api_*.svg $version/
else
  echo "⚠️ Warning: No UML diagrams found."
fi

# Detect latest stable version
latest_stable=$(ls -d [0-9]*/ 2>/dev/null | grep -v SNAPSHOT | cut -f1 -d'/' | sort -Vr | head -n1)
echo "📌 Latest stable version detected: $latest_stable"

if [ ! -z "$latest_stable" ]; then
   echo "🔄 Updating latest stable documentation..."
   rm -rf latest-stable
   mkdir -p latest-stable
   cp -rf "$latest_stable"/* latest-stable/

   echo "📝 Creating robots.txt file..."
   cat > robots.txt << EOF
User-agent: *
Allow: /
Allow: /latest-stable/
Disallow: /*/[0-9]*/
EOF
fi

# Update version list
echo "📜 Updating version list..."
sorted_dirs=$(ls -d [0-9]*/ 2>/dev/null | cut -f1 -d'/' | sort -Vr)

echo "| Version | Documentation |" > list_versions.md
echo "|:---:|---|" >> list_versions.md

for directory in $sorted_dirs; do
  diagrams=""
  for diagram in `ls $directory/api_*.svg 2>/dev/null | cut -f2 -d'/'`; do
    name=`echo "$diagram" | tr _ " " | cut -f1 -d'.' | sed -r 's/^api/API/g'`
    diagrams="$diagrams<br>[$name]($directory/$diagram)"
  done
  if [ "$directory" = "$latest_stable" ]; then
    echo "| **$directory (latest stable)** | [API documentation](latest-stable)$diagrams |" >> list_versions.md
  else
    echo "| $directory | [API documentation]($directory)$diagrams |" >> list_versions.md
  fi
done

# Secure commit and push
if [ -z "$GITHUB_TOKEN" ]; then
  echo "❌ Error: GITHUB_TOKEN is not set. Aborting push."
  exit 1
fi

git add -A
git config user.email "${repository_name}-bot@eclipse.org"
git config user.name "Eclipse Keyple Bot"
git commit --allow-empty -m "docs: update documentation for version $version"
git remote set-url origin "https://x-access-token:${GITHUB_TOKEN}@github.com/eclipse-keyple/${repository_name}.git"
git push origin HEAD:doc

echo "✅ Documentation update completed."
