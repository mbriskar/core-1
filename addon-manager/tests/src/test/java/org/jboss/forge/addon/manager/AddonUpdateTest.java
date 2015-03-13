package org.jboss.forge.addon.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.manager.impl.ui.AddonCommandConstants;
import org.jboss.forge.addon.manager.impl.ui.AddonUpdateCommand;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.manager.maven.MavenContainer;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AddonUpdateTest
{
   private static String previousUserSettings;
   private static String previousLocalRepository;

   @BeforeClass
   public static void setRemoteRepository() throws IOException
   {
      previousUserSettings = System.setProperty(MavenContainer.ALT_USER_SETTINGS_XML_LOCATION,
               getAbsolutePath("profiles/settings.xml"));
      previousLocalRepository = System.setProperty(MavenContainer.ALT_LOCAL_REPOSITORY_LOCATION,
               "target/the-other-repository");
   }

   private static String getAbsolutePath(String path) throws FileNotFoundException
   {
      URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
      if (resource == null)
         throw new FileNotFoundException(path);
      return resource.getFile();
   }

   @AfterClass
   public static void clearRemoteRepository()
   {
      if (previousUserSettings == null)
      {
         System.clearProperty(MavenContainer.ALT_USER_SETTINGS_XML_LOCATION);
      }
      else
      {
         System.setProperty(MavenContainer.ALT_USER_SETTINGS_XML_LOCATION, previousUserSettings);
      }
      if (previousLocalRepository == null)
      {
         System.clearProperty(MavenContainer.ALT_LOCAL_REPOSITORY_LOCATION);
      }
      else
      {
         System.setProperty(MavenContainer.ALT_LOCAL_REPOSITORY_LOCATION, previousUserSettings);
      }
   }

   @Deployment
   @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.addon:ui"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
            @AddonDependency(name = "org.jboss.forge.addon:addon-manager"),
            @AddonDependency(name = "org.jboss.forge.addon:git", version = "2.15.0.Final")
   })
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap.create(AddonArchive.class).addBeansXML();
   }

   @Inject
   private UITestHarness uiTestHarness;
   @Inject
   private Furnace furnace;

   @Test
   public void testUpdateAddon() throws Exception
   {
      try (CommandController controller = uiTestHarness.createCommandController(AddonUpdateCommand.class))
      {
         controller.initialize();
         UICommandMetadata metadata = controller.getMetadata();
         assertEquals(AddonCommandConstants.ADDON_UPDATE_COMMAND_NAME, metadata.getName());
         assertEquals(1, controller.getInputs().size());
         controller.setValueFor("named", "org.jboss.forge.addon:git");
         assertTrue(controller.canExecute());
         Result result = controller.execute();
         Assert.assertNotNull(result);
         Assert.assertFalse(result instanceof Failed);
         boolean found = false;
         String desiredAddonIdName = "org.jboss.forge.addon:git";
         for (AddonRepository repository : furnace.getRepositories())
         {
            List<AddonId> allAddonsInRep = repository.listAll();
            for (AddonId addonId : allAddonsInRep)
            {
               if (addonId.getName().equals(desiredAddonIdName))
               {
                  // there is the git addon
                  if (addonId.getVersion().compareTo(new SingleVersion("2.15.0.Final")) > 0)
                  {
                     // the addon is of newer version
                     found = true;
                  }
               }
            }
         }
         if (!found)
         {
            fail();
         }
      }
   }
}
