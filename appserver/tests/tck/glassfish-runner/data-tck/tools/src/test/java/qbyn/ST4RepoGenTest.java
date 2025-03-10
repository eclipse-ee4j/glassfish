/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package qbyn;

import ee.jakarta.tck.data.tools.annp.RepositoryInfo;
import ee.jakarta.tck.data.tools.qbyn.ParseUtils;
import ee.jakarta.tck.data.tools.qbyn.QueryByNameInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ee.jakarta.tck.data.tools.annp.AnnProcUtils.TCK_IMPORTS;
import static ee.jakarta.tck.data.tools.annp.AnnProcUtils.TCK_OVERRIDES;

public class ST4RepoGenTest {
    static String REPO_TEMPLATE = """
            import jakarta.annotation.Generated;
            import jakarta.data.repository.OrderBy;
            import jakarta.data.repository.Query;
            import jakarta.data.repository.Repository;
            import #repo.fqn#;

            @Repository(dataStore = "#repo.dataStore#")
            @Generated("ee.jakarta.tck.data.tools.annp.RespositoryProcessor")
            public interface #repo.name#$ extends #repo.name# {
                #repo.methods :{m |
                    @Override
                    @Query("#m.query#")
                    #m.orderBy :{o | @OrderBy(value="#o.property#", descending = #o.descending#)}#
                    public #m.returnType# #m.name# (#m.parameters: {p | #p#}; separator=", "#);
                    
                    }
            	#
            }
            """;
    @Test
    public void testSyntax() {
        List<String> methods = Arrays.asList("findByFloorOfSquareRootOrderByIdAsc", "findByHexadecimalIgnoreCase",
                "findById", "findByIdBetween", "findByHexadecimalIgnoreCaseBetweenAndHexadecimalNotIn");
        ST s = new ST( "<methods :{m | public X <m> ();\n}>");
        s.add("methods", methods);
        System.out.println(s.render());
    }

    private RepositoryInfo createRepositoryInfo() {
        RepositoryInfo repo = new RepositoryInfo();
        repo.setFqn("org.acme.BookRepository");
        repo.setName("BookRepository");
        repo.setDataStore("book");

        RepositoryInfo.MethodInfo findByTitleLike = new RepositoryInfo.MethodInfo("findByTitleLike", "List<Book>", "from Book where title like :title", null);
        findByTitleLike.addParameter("String title");
        repo.addMethod(findByTitleLike);
        RepositoryInfo.MethodInfo findByNumericValue = new RepositoryInfo.MethodInfo("findByNumericValue", "Optional<AsciiCharacter>",
                "from AsciiCharacter where numericValue = :numericValue",
                Collections.singletonList(new QueryByNameInfo.OrderBy("numericValue", QueryByNameInfo.OrderBySortDirection.ASC)));
        findByNumericValue.addParameter("int id");
        repo.addMethod(findByNumericValue);
        return repo;
    }
    @Test
    public void testRepoGen() {
        RepositoryInfo repo = createRepositoryInfo();
        ST st = new ST(REPO_TEMPLATE, '#', '#');
        st.add("repo", repo);
        System.out.println(st.render());
    }

    @Test
    public void testRepoGenViaGroupFiles() {
        STGroup repoGroup = new STGroupFile("RepoTemplate.stg");
        ST genRepo = repoGroup.getInstanceOf("genRepo");
        RepositoryInfo repo = createRepositoryInfo();
        genRepo.add("repo", repo);
        String classSrc = genRepo.render();
        System.out.println(classSrc);
        Assertions.assertTrue(classSrc.contains("interface BookRepository$"));
        Assertions.assertTrue(classSrc.contains("// TODO; Implement TCK overrides"));
    }

    @Test
    public void testRepoGenWithTckOverride() {
        STGroup repoGroup = new STGroupFile("RepoTemplate.stg");
        repoGroup.defineTemplate("tckImports", "import jakarta.data.Delete;\n");
        repoGroup.defineTemplate("tckOverrides", "@Delete\nvoid deleteAllBy();\n");
        ST genRepo = repoGroup.getInstanceOf("genRepo");
        RepositoryInfo repo = createRepositoryInfo();
        genRepo.add("repo", repo);
        String classSrc = genRepo.render();
        System.out.println(classSrc);
        Assertions.assertTrue(classSrc.contains("interface BookRepository$"));
        Assertions.assertTrue(!classSrc.contains("// TODO; Implement TCK overrides"));
        Assertions.assertTrue(classSrc.contains("void deleteAllBy();"));
        Assertions.assertTrue(classSrc.contains("import jakarta.data.Delete;"));
    }

    @Test
    public void testRepoGenWithTckOverrideFromImport() {
        STGroup repoGroup = new STGroupFile("RepoTemplate.stg");
        STGroup tckGroup = new STGroupFile("org.acme.BookRepository_tck.stg");
        tckGroup.importTemplates(repoGroup);
        ST genRepo = tckGroup.getInstanceOf("genRepo");
        long count = tckGroup.getTemplateNames().stream().filter(t -> t.equals(TCK_IMPORTS) | t.equals(TCK_OVERRIDES)).count();
        System.out.printf("tckGroup.templates(%d) %s\n", count, tckGroup.getTemplateNames());
        System.out.printf("tckGroup: %s\n", tckGroup.show());

        RepositoryInfo repo = createRepositoryInfo();
        genRepo.add("repo", repo);
        String classSrc = genRepo.render();
        System.out.println(classSrc);
        Assertions.assertTrue(classSrc.contains("interface BookRepository$"));
        Assertions.assertTrue(!classSrc.contains("// TODO; Implement TCK overrides"));
        Assertions.assertTrue(classSrc.contains("void deleteAllBy();"));
        Assertions.assertTrue(classSrc.contains("import jakarta.data.Delete;"));
    }

    @Test
    public void testMissingGroupTemplate() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            STGroup repoGroup = new STGroupFile("Rectangles_tck.stg");
            repoGroup.getTemplateNames();
        });
        Assertions.assertNotNull(ex, "Load of Rectangles_tck should fail");
    }
}
