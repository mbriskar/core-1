/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.addon.validation.ui;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.validate.UIValidator;

/**
 * A {@link UIValidator} adapter that validates using Bean Validation 1.1
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class UIValidationAdapter implements UIValidator
{
   private final Validator validator;
   private final InputComponent<?, ?> input;
   private final Class<?> commandClass;

   public UIValidationAdapter(Validator validator, InputComponent<?, ?> input, Class<?> commandClass)
   {
      super();
      this.validator = validator;
      this.input = input;
      this.commandClass = commandClass;
   }

   @Override
   public void validate(UIValidationContext context)
   {
      for (ConstraintViolation<?> violation : validator.validateValue(commandClass, input.getName(), input))
      {
         context.addValidationError(input, violation.getMessage());
      }
   }
}
