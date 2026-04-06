const fs = require('fs');
const path = require('path');

const dirPath = path.join(__dirname, 'front-end', 'src', 'views', 'high');

function processFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf8');
  let modified = false;

  // 替换所有 dark:bg-gray-xxx 为 dark:bg-black
  const patterns = [
    /dark:bg-gray-\d+\/?\d*/g,
    /dark:bg-slate-\d+\/?\d*/g,
    /dark:bg-white\/\d+/g,
    /dark:bg-indigo-\d+\/\d+/g,
    /dark:bg-emerald-\d+\/\d+/g,
    /dark:bg-amber-\d+\/\d+/g,
    /dark:bg-blue-\d+\/\d+/g,
    /dark:bg-purple-\d+\/\d+/g,
    /dark:bg-pink-\d+\/\d+/g,
    /dark:bg-green-\d+\/\d+/g,
    /dark:bg-yellow-\d+\/\d+/g,
    /dark:bg-orange-\d+\/\d+/g,
    /dark:bg-red-\d+\/\d+/g,
    /dark:bg-cyan-\d+\/\d+/g,
    /dark:bg-teal-\d+\/\d+/g,
    /dark:bg-lime-\d+\/\d+/g,
    /dark:bg-rose-\d+\/\d+/g,
    /dark:bg-violet-\d+\/\d+/g,
    /dark:bg-fuchsia-\d+\/\d+/g
  ];

  patterns.forEach(pattern => {
    if (pattern.test(content)) {
      content = content.replace(pattern, 'dark:bg-black');
      modified = true;
    }
  });

  if (modified) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log('Modified:', path.relative(dirPath, filePath));
  }
}

function walkDirectory(dir) {
  const files = fs.readdirSync(dir);
  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    if (stat.isDirectory()) {
      walkDirectory(filePath);
    } else if (file.endsWith('.vue')) {
      processFile(filePath);
    }
  });
}

console.log('Processing files...');
walkDirectory(dirPath);
console.log('Done!');
