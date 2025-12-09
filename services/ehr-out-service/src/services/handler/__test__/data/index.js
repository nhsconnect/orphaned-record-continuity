import path from 'path';
import { readFileSync } from 'fs';

export const loadTestData = (relativePath, parseJson = true) => {
  const filePath = path.join(__dirname, relativePath);
  const fileContent = readFileSync(filePath, 'utf-8');
  return parseJson ? JSON.parse(fileContent) : fileContent;
};
