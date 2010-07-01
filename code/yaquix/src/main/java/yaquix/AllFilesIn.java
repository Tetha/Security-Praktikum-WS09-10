/* Copyright 2010 FSOA0910. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY FSOA0910 ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL FSOA0910 OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of FSOA0910.
 */
package yaquix;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Class comment
 *
 * @author harald
 */
public class AllFilesIn implements Iterable<File> {
    private File iteratedDirectory;

    public AllFilesIn(String directoryName) {
        this(new File(directoryName));
    }

    public AllFilesIn(File iteratedDirectory) {
        this.iteratedDirectory = iteratedDirectory;
    }

    @Override
    public Iterator<File> iterator() {
        return filesDeeplyIn(iteratedDirectory).iterator();
    }

    private List<File> filesDeeplyIn(File file) {
        if (file.isDirectory())
            return collectSubFilesIn(file);
        else
            return Collections.singletonList(file);
    }

    private List<File> collectSubFilesIn(File directory) {
        assert directory.isDirectory();
        List<File> subFiles = new LinkedList<File>();
        for (File subFile : directory.listFiles())
            subFiles.addAll(filesDeeplyIn(subFile));

        return subFiles;
    }
}
