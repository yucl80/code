#include <iostream>
#include <clang-c/Index.h>
using namespace std;

ostream& operator<<(ostream& stream, const CXString& str)
{
  stream << clang_getCString(str);
  clang_disposeString(str);
  return stream;
}

void printClassAndMethods(CXCursor cursor)
{
  if (clang_getCursorKind(cursor) == CXCursor_CXXMethod || clang_getCursorKind(cursor) == CXCursor_FunctionDecl)
  {
    cout << "Method: " << clang_getCursorSpelling(cursor) << endl;

    // 获取方法调用的其他方法
    clang_visitChildren(
      cursor,
      [](CXCursor c, CXCursor parent, CXClientData client_data)
      {
        if (clang_getCursorKind(c) == CXCursor_CallExpr)
        {
          CXCursor referencedCursor = clang_getCursorReferenced(c);
          CXCursor methodCursor = clang_getCursorDefinition(referencedCursor);
          CXCursor classCursor = clang_getCursorSemanticParent(methodCursor);
          if (clang_getCursorKind(classCursor) == CXCursor_ClassDecl) {
            CXString className = clang_getCursorSpelling(classCursor);
            if (clang_getCString(className) != nullptr && clang_getCString(className)[0] != '\0') {
              cout << "  Calls: " << clang_getCursorSpelling(referencedCursor) << " (Class: " << className << ")" << endl;
            } else {
              cout << "  Calls: " << clang_getCursorSpelling(referencedCursor) << endl;
            }      
          
             
          } else {
            CXString fileName = clang_getCursorSpelling(classCursor);
            if (clang_getCString(fileName) != nullptr && clang_getCString(fileName)[0] != '\0') {
                cout << "  Calls: " << clang_getCursorSpelling(referencedCursor) <<" (File:"  << fileName << ")" << endl;
             }
          }
        }
        return CXChildVisit_Recurse;
      },
      nullptr);
  }
  else if (clang_getCursorKind(cursor) == CXCursor_ClassDecl)
  {
    cout << "Class: " << clang_getCursorSpelling(cursor) << endl;
  }

  clang_visitChildren(
    cursor,
    [](CXCursor c, CXCursor parent, CXClientData client_data)
    {
      printClassAndMethods(c);
      return CXChildVisit_Continue;
    },
    nullptr);
}

int main()
{
  CXIndex index = clang_createIndex(0, 0);
  CXTranslationUnit unit = clang_parseTranslationUnit(
    index,
    "test.cpp", nullptr, 0,
    nullptr, 0,
    CXTranslationUnit_None);
  if (unit == nullptr)
  {
    cerr << "Unable to parse translation unit. Quitting." << endl;
    exit(-1);
  }

  CXCursor cursor = clang_getTranslationUnitCursor(unit);
  printClassAndMethods(cursor);

  clang_disposeTranslationUnit(unit);
  clang_disposeIndex(index);
}
