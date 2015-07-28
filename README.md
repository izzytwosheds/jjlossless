# jjlossless
Platform independent Java implementation of JPEG LOSSLESS codec. Very light weight and "core Java".

This is a very early version, targeted primarily at decoding monochrome lossless JPEG images (popular in medical imaging).

Current feature set:
 - Decoding only, no encoding support (yet)
 - Monochrome (single component) images only, no support for color images (yet)
 - No support for restart intervals (yet)
 - Support for variety of sources (byte[], file path, File, InputStream)
 - Command line support (exports JPEG into PGM)
